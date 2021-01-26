package net.boomerangplatform.service;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.CloudEvent;
import io.cloudevents.json.Json;
import io.cloudevents.v1.AttributesImpl;
import io.cloudevents.v1.CloudEventBuilder;
import io.cloudevents.v1.CloudEventImpl;
import net.boomerangplatform.client.NatsClient;
import net.boomerangplatform.client.WorkflowClient;
import net.boomerangplatform.model.CustomAttributeExtension;

@Service
public class EventProcessorImpl implements EventProcessor {

  private static final Logger logger = LogManager.getLogger(EventProcessorImpl.class);

  private static final String TYPE_PREFIX = "io.boomerang.eventing.";

  @Value("${eventing.nats.enabled}")
  private Boolean natsEnabled;

  @Value("${eventing.auth.enabled}")
  private Boolean authorizationEnabled;

  @Autowired
  private NatsClient natsClient;

  @Autowired
  private WorkflowClient workflowClient;

  @Override
  public HttpStatus routeWebhookEvent(String token, String requestUri, String trigger, String workflowId,
      JsonNode payload, String workflowActivityId, String topic, String status) {
    final String eventId = UUID.randomUUID().toString();
    final String eventType = TYPE_PREFIX + trigger;
    final URI uri = URI.create(requestUri);
    String subject = "/" + workflowId;
    if ("wfe".equals(trigger) && workflowActivityId != null) {
      subject = subject + "/" + workflowActivityId + "/" + topic;
    }

    if (!checkAccess(workflowId, token)) {
      return HttpStatus.FORBIDDEN;
    }
    
    if (!"failure".equals(status)) {
      status = "success";
    }
    CustomAttributeExtension statusCAE = new CustomAttributeExtension("status", status);

    final CloudEventImpl<JsonNode> cloudEvent = CloudEventBuilder.<JsonNode>builder().withType(eventType).withExtension(statusCAE)
        .withId(eventId).withSource(uri).withData(payload).withSubject(subject).withTime(ZonedDateTime.now()).build();

    final String jsonPayload = Json.encode(cloudEvent);
    logger.info("CloudEvent Object - " + jsonPayload);

    if (natsEnabled) {
      natsClient.publish(eventId, jsonPayload);
    } else {
      workflowClient.executeWorkflowPut(cloudEvent);
    }

    return HttpStatus.OK;
  }

  @Override
  public HttpStatus routeCloudEvent(CloudEvent<AttributesImpl, JsonNode> cloudEvent, String token, URI uri) {

    logger.info("routeCloudEvent() - Received CloudEvent Attributes: " + cloudEvent.getAttributes());
    logger.info("routeCloudEvent() - Received CloudEvent Data: " + cloudEvent.getData().get());

    String eventId = UUID.randomUUID().toString();
    String eventType = TYPE_PREFIX + "custom";
    String subject = cloudEvent.getAttributes().getSubject().orElse("");

    if (!subject.startsWith("/") || cloudEvent.getData().isEmpty()) {
      return HttpStatus.BAD_REQUEST;
    }

    if (!checkAccess(getWorkflowIdFromSubject(subject), token)) {
      return HttpStatus.FORBIDDEN;
    }
    
    String status = "success";
    if (cloudEvent.getExtensions() != null && cloudEvent.getExtensions().containsKey("status")) {
      if ("failure".equals((String) cloudEvent.getExtensions().get("status"))) {
        status = "failure";
      }
    }
    CustomAttributeExtension statusCAE = new CustomAttributeExtension("status", status);

    final CloudEventImpl<JsonNode> forwardedCloudEvent = CloudEventBuilder.<JsonNode>builder().withType(eventType).withExtension(statusCAE)
        .withId(eventId).withSource(uri).withData(cloudEvent.getData().get()).withSubject(subject)
        .withTime(ZonedDateTime.now()).build();

    logger.info("routeCloudEvent() - Forwarded CloudEvent Data: " + forwardedCloudEvent.getData().get());

    if (natsEnabled) {
      natsClient.publish(eventId, forwardedCloudEvent.getData().get().toString());
    } else {
      workflowClient.executeWorkflowPut(forwardedCloudEvent);
    }

    return HttpStatus.OK;
  }

  private Boolean checkAccess(String workflowId, String token) {
    if (authorizationEnabled) {
      logger.info("checkAccess() - Token: " + token);
      if (token != null) {
        return workflowClient.validateWorkflowToken(workflowId, token);
      } else {
        logger.error("checkAccess() - Error: no token provided.");
        return false;
      }
    } else {
      return true;
    }
  }

  private String getWorkflowIdFromSubject(String subject) {
    // Reference 0 will be an empty string as it is the left hand side of the split
    String[] splitArr = subject.split("/");
    if (splitArr.length >= 2) {
      return splitArr[1].toString();
    } else {
      logger.error("processCloudEvent() - Error: No workflow ID found in event");
      return "";
    }
  }
}
