package io.boomerang.service;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import io.boomerang.client.NatsClient;
import io.boomerang.client.WorkflowClient;
import io.boomerang.model.CustomAttributeExtension;
import io.cloudevents.CloudEvent;
import io.cloudevents.json.Json;
import io.cloudevents.v1.AttributesImpl;
import io.cloudevents.v1.CloudEventBuilder;
import io.cloudevents.v1.CloudEventImpl;

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
  public ResponseEntity<CloudEvent<AttributesImpl, JsonNode>>  routeWebhookEvent(String token, String requestUri, String trigger, String workflowId,
      JsonNode payload, String workflowActivityId, String topic, String status) {
    //Validate Token and WorkflowID. Do first.
    HttpStatus accessStatus = checkAccess(workflowId, token);
    if (accessStatus != HttpStatus.OK) {
      return ResponseEntity.status(accessStatus).build();
    }
    
    final String eventId = UUID.randomUUID().toString();
    final String eventType = TYPE_PREFIX + trigger;
    final URI uri = URI.create(requestUri);
    String subject = "/" + workflowId;
    
    //Validate WFE Attributes
    if ("wfe".equals(trigger) && workflowActivityId != null) {
      subject = subject + "/" + workflowActivityId + "/" + topic;
    } else if ("wfe".equals(trigger)) {
      //WFE requires workflowActivityId
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    
    if (!"failure".equals(status)) {
      status = "success";
    }
    CustomAttributeExtension statusCAE = new CustomAttributeExtension("status", status);

    final CloudEventImpl<JsonNode> cloudEvent = CloudEventBuilder.<JsonNode>builder().withType(eventType).withExtension(statusCAE)
        .withId(eventId).withSource(uri).withData(payload).withSubject(subject).withTime(ZonedDateTime.now()).build();

    final String jsonPayload = Json.encode(cloudEvent);
    logger.debug("routeWebhookEvent() - CloudEvent: " + jsonPayload);

    if (natsEnabled) {
      natsClient.publish(eventId, jsonPayload);
    } else {
      workflowClient.executeWorkflowPut(cloudEvent);
    }

    return ResponseEntity.ok().body(cloudEvent);
  }

  @Override
  public ResponseEntity<CloudEvent<AttributesImpl, JsonNode>> routeCloudEvent(CloudEvent<AttributesImpl, JsonNode> cloudEvent, String token, URI uri) {
    //Validate Token and WorkflowID. Do first.
    String subject = cloudEvent.getAttributes().getSubject().orElse("");
    if (!subject.startsWith("/") || cloudEvent.getData().isEmpty()) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
    HttpStatus accessStatus = checkAccess(getWorkflowIdFromSubject(subject), token);
    if (accessStatus != HttpStatus.OK) {
      return ResponseEntity.status(accessStatus).build();
    }
    
    logger.debug("routeCloudEvent() - CloudEvent Attributes: " + cloudEvent.getAttributes());
    logger.debug("routeCloudEvent() - CloudEvent Data: " + cloudEvent.getData().get());

    String eventId = UUID.randomUUID().toString();
    String eventType = TYPE_PREFIX + "custom";
    
    String status = "success";
    if (cloudEvent.getExtensions() != null && cloudEvent.getExtensions().containsKey("status")) {
      String statusExtension = cloudEvent.getExtensions().get("status").toString();
      if ("failure".equals(statusExtension)) {
        status = statusExtension;
      }
    }
    CustomAttributeExtension statusCAE = new CustomAttributeExtension("status", status);

    final CloudEventImpl<JsonNode> forwardedCloudEvent = CloudEventBuilder.<JsonNode>builder().withType(eventType).withExtension(statusCAE)
        .withId(eventId).withSource(uri).withData(cloudEvent.getData().get()).withSubject(subject)
        .withTime(ZonedDateTime.now()).build();

    if (natsEnabled) {
      natsClient.publish(eventId, forwardedCloudEvent.getData().get().toString());
    } else {
      workflowClient.executeWorkflowPut(forwardedCloudEvent);
    }

    return ResponseEntity.ok().body(forwardedCloudEvent);
  }

  private HttpStatus checkAccess(String workflowId, String token) {
    if (authorizationEnabled) {
      logger.debug("checkAccess() - Token: " + token);
      if (token != null && !token.isEmpty() && workflowId!= null&& !workflowId.isEmpty()) {
        return workflowClient.validateWorkflowToken(workflowId, token);
      } else {
        logger.error("checkAccess() - Error: no token provided.");
        return HttpStatus.UNAUTHORIZED;
      }
    } else {
      return HttpStatus.OK;
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
