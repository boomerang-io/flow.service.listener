package io.boomerang.service;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.boomerang.client.WorkflowClient;
import io.boomerang.eventing.nats.jetstream.PubOnlyTunnel;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;

@Service
public class EventProcessorImpl implements EventProcessor {

  private static final Logger logger = LogManager.getLogger(EventProcessorImpl.class);

  private static final String TYPE_PREFIX = "io.boomerang.eventing.";

  @Value("${eventing.auth.enabled:false}")
  private Boolean authorizationEnabled;

  @Value("${eventing.jetstream.stream.subject:#{null}}")
  private String jetstreamStreamSubject;

  @Autowired(required = false)
  private Optional<PubOnlyTunnel> pubOnlyTunnel;

  @Autowired
  private WorkflowClient workflowClient;

  @Override
  public ResponseEntity<CloudEvent> routeWebhookEvent(String token,
      String requestUri, String trigger, String workflowId, JsonNode payload,
      String workflowActivityId, String topic, String status) {

    // Validate Token and WorkflowID. Do first.
    HttpStatus accessStatus = checkAccess(workflowId, token);

    if (accessStatus != HttpStatus.OK) {
      return ResponseEntity.status(accessStatus).build();
    }

    final String eventId = UUID.randomUUID().toString();
    final String eventType = TYPE_PREFIX + trigger;
    final URI uri = URI.create(requestUri);
    String subject = "/" + workflowId;

    // Validate WFE Attributes
    if ("wfe".equals(trigger) && workflowActivityId != null) {
      subject = subject + "/" + workflowActivityId + "/" + topic;
    } else if ("wfe".equals(trigger)) {

      // WFE requires workflowActivityId
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    if (!"failure".equals(status)) {
      status = "success";
    }

    try {
    // @formatter:off
    CloudEvent cloudEvent = CloudEventBuilder.v03()
        .withId(eventId)
        .withSource(uri)
        .withSubject(subject)
        .withType(eventType)
        .withExtension("status", status)
        .withTime(OffsetDateTime.now())
        .withData(MediaType.APPLICATION_JSON_VALUE, new ObjectMapper().writer().writeValueAsBytes(payload))
        .build();
    // @formatter:on

    logger.debug("routeWebhookEvent() - CloudEvent: " + cloudEvent);

    forwardCloudEvent(cloudEvent);

    return ResponseEntity.ok().body(cloudEvent);
  } catch (JsonProcessingException jpEx) {
    logger.debug("JSON Processing failed for: " + payload);
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
  }
  }

  @Override
  public ResponseEntity<CloudEvent> routeCloudEvent(
      CloudEvent cloudEvent, String token, URI uri) {

    // Validate Token and WorkflowID. Do first.
    String subject = cloudEvent.getSubject();

    if (!subject.startsWith("/") || cloudEvent.getData() == null) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    HttpStatus accessStatus = checkAccess(getWorkflowIdFromSubject(subject), token);
    if (accessStatus != HttpStatus.OK) {
      return ResponseEntity.status(accessStatus).build();
    }

    logger.debug("routeCloudEvent() - CloudEvent Attributes: " + cloudEvent.getAttributeNames());
    logger.debug("routeCloudEvent() - CloudEvent Data: " + cloudEvent.getData());

    String eventId = UUID.randomUUID().toString();
    String eventType = TYPE_PREFIX + "custom";

    String status = "success";
    if (cloudEvent.getExtensionNames() != null
        && cloudEvent.getExtensionNames().contains("status")) {
      String statusExtension = cloudEvent.getExtension("status").toString();
      if ("failure".equals(statusExtension)) {
        status = statusExtension;
      }
    }
    
    // @formatter:off
    CloudEvent forwardedCloudEvent = CloudEventBuilder.v03()
        .withId(eventId)
        .withSource(uri)
        .withSubject(subject)
        .withType(eventType)
        .withExtension("status", status)
        .withTime(OffsetDateTime.now())
        .withData(cloudEvent.getData())
        .build();
    // @formatter:on

    forwardCloudEvent(forwardedCloudEvent);

    return ResponseEntity.ok().body(forwardedCloudEvent);
  }

  private HttpStatus checkAccess(String workflowId, String token) {
    if (authorizationEnabled) {
      logger.debug("checkAccess() - Token: " + token);

      if (token != null && !token.isEmpty() && workflowId != null && !workflowId.isEmpty()) {
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

  private void forwardCloudEvent(CloudEvent cloudEvent) {
    // If eventing is enabled, try to send the cloud event to it
    try {
      String serializedCloudEvent = new String(EventFormatProvider.getInstance()
          .resolveFormat(JsonFormat.CONTENT_TYPE).serialize(cloudEvent));
      pubOnlyTunnel.orElseThrow().publish(jetstreamStreamSubject, serializedCloudEvent);
    } catch (Exception e) {

      // The code will get to this point only if eventing is disabled or if it
      // for some reason it failed to publish the message
      workflowClient.executeWorkflowPut(cloudEvent);
    }
  }
}
