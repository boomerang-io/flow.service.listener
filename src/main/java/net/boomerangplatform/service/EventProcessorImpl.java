package net.boomerangplatform.service;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Map;
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
import io.cloudevents.v1.http.Unmarshallers;
import net.boomerangplatform.client.NatsClient;
import net.boomerangplatform.client.WorkflowClient;

@Service
public class EventProcessorImpl implements EventProcessor {

  private static final Logger logger = LogManager.getLogger(EventProcessorImpl.class);

  private static final String TYPE_PREFIX = "io.boomerang.eventing.";

  @Value("${eventing.enabled}")
  private Boolean eventingEnabled;

  @Value("${eventing.auth.enabled}")
  private Boolean authorizationEnabled;

  @Autowired
  private NatsClient natsClient;

  @Autowired
  private WorkflowClient workflowClient;

  @Override
  public HttpStatus routeWebhookEvent(String token, String requestUri, String trigger, String workflowId,
      JsonNode payload, String workflowActivityId, String topic ) {
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

    final CloudEventImpl<JsonNode> cloudEvent = CloudEventBuilder.<JsonNode>builder().withType(eventType)
        .withId(eventId).withSource(uri).withData(payload).withSubject(subject).withTime(ZonedDateTime.now()).build();

    final String jsonPayload = Json.encode(cloudEvent);
    logger.info("CloudEvent Object - " + jsonPayload);

    if (eventingEnabled) {
      natsClient.publish(eventId, jsonPayload);
    } else {
      workflowClient.executeWorkflowPut(cloudEvent);
    }

    return HttpStatus.OK;
  }

  @Override
  public HttpStatus routeCloudEvent(String token, String requestUri, Map<String, Object> headers, JsonNode payload) {

    logger.info("routeCloudEvent() - Event as Message String: " + payload.toString());

    CloudEvent<AttributesImpl, JsonNode> event = Unmarshallers.structured(JsonNode.class).withHeaders(() -> headers)
        .withPayload(() -> payload.toString()).unmarshal();

    logger.info("routeCloudEvent() - Attributes: " + event.getAttributes().toString());
    JsonNode eventData = event.getData().get();
    logger.info("routeCloudEvent() - Data: " + eventData.toPrettyString());

    final String eventId = UUID.randomUUID().toString();
    final String eventType = TYPE_PREFIX + "custom";
    final URI uri = URI.create(requestUri);
    String subject = event.getAttributes().getSubject().orElse("");

    if (!subject.startsWith("/")) {
      return HttpStatus.BAD_REQUEST;
    }

    if (!checkAccess(getWorkflowIdFromSubject(subject), token)) {
      return HttpStatus.FORBIDDEN;
    }

    final CloudEventImpl<JsonNode> cloudEvent = CloudEventBuilder.<JsonNode>builder().withType(eventType)
        .withId(eventId).withSource(uri).withData(payload).withSubject(subject).withTime(ZonedDateTime.now()).build();

    final String jsonPayload = Json.encode(cloudEvent);
    logger.info("CloudEvent Object - " + jsonPayload);

    if (eventingEnabled) {
      natsClient.publish(eventId, jsonPayload);
    } else {
      workflowClient.executeWorkflowPut(cloudEvent);
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
