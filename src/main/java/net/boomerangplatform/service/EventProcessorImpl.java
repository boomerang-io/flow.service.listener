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

  protected static final String TYPE_PREFIX = "io.boomerang.eventing.";
  
  protected static final String SUBJECT = "flow-workflow-execute";
  
  @Value("${eventing.enabled}")
  private Boolean eventingEnabled;

  @Value("${eventing.auth.enabled}")
  private Boolean authzEnabled;

  @Autowired
  private NatsClient natsClient;
  
  @Autowired
  private WorkflowClient workflowClient;

  @Override
  public HttpStatus routeEvent(String token, String requestUri, String trigger, String workflowId, JsonNode payload) {
    final String eventId = UUID.randomUUID().toString();
    final String eventType = TYPE_PREFIX + trigger;
    final URI uri = URI.create(requestUri);
    final String subject = "/" + workflowId;
    
//    if (!checkAccess(workflowId, trigger, token)) {
//      return HttpStatus.FORBIDDEN;
//    }
        
    final CloudEventImpl<JsonNode> cloudEvent =
    CloudEventBuilder.<JsonNode>builder()
      .withType(eventType)
      .withId(eventId)
      .withSource(uri)
      .withData(payload)
      .withSubject(subject)
      .withTime(ZonedDateTime.now())
      .build();

    final String jsonPayload = Json.encode(cloudEvent);
    logger.info("CloudEvent Object - " + jsonPayload);
    if (eventingEnabled) {
      natsClient.publish(eventId, SUBJECT, jsonPayload);
    } else {
      workflowClient.executeWorkflowPut(SUBJECT, cloudEvent);
    }
    
    return HttpStatus.OK;
  }
  
  @Override
  public HttpStatus routeCloudEvent(String token, String requestUri, Map<String, Object> headers, JsonNode payload) {

    logger.info("routeCloudEvent() - Event as Message String: " + payload.toString());
    
    CloudEvent<AttributesImpl, JsonNode> event = Unmarshallers.structured(JsonNode.class)
        .withHeaders(() -> headers).withPayload(() -> payload.toString()).unmarshal();

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
    
    if (!checkAccess(getWorkflowIdFromSubject(subject), "custom", token)) {
      return HttpStatus.FORBIDDEN;
    }
    
    final CloudEventImpl<JsonNode> cloudEvent =
    CloudEventBuilder.<JsonNode>builder()
      .withType(eventType)
      .withId(eventId)
      .withSource(uri)
      .withData(payload)
      .withSubject(subject)
      .withTime(ZonedDateTime.now())
      .build();

    final String jsonPayload = Json.encode(cloudEvent);
    logger.info("CloudEvent Object - " + jsonPayload);
    if (eventingEnabled) {
      natsClient.publish(eventId, SUBJECT, jsonPayload);
    } else {
      workflowClient.executeWorkflowPut(SUBJECT, cloudEvent);
    }
    
    return HttpStatus.OK;
  }
  
  //@RequestHeader("Authorization") String token
  //@RequestParam("access_token") String token
  //TODO replace with SecurityConfig and SecurityFilter
  private Boolean checkAccess(String workflowId, String trigger, String token) {
    if (authzEnabled) {
      logger.info("checkAccess() - Token: " + token);
      if (token != null) {
        return workflowClient.validateTriggerToken(workflowId, trigger, token);
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
