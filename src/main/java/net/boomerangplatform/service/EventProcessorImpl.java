package net.boomerangplatform.service;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  protected static final String TYPE_PREFIX = "io.boomerang.eventing.";
  
  protected static final String SUBJECT = "flow-workflow-execute";
  
  @Value("${eventing.enabled}")
  private Boolean eventingEnabled;

  private static final Logger logger = LogManager.getLogger(EventProcessorImpl.class);

  @Autowired
  private NatsClient natsClient;
  
  @Autowired
  private WorkflowClient wfClient;

  @Override
  public void routeEvent(String requestUri, String target, String workflowId, JsonNode payload) {
    final String eventId = UUID.randomUUID().toString();
    final String eventType = TYPE_PREFIX + target;
    final URI uri = URI.create(requestUri);
    final String subject = "/" + workflowId;
        
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
      wfClient.executeWorkflowPut(SUBJECT, cloudEvent);
    }
  }
  
  @Override
  public void routeCloudEvent(String requestUri, Map<String, Object> headers, JsonNode payload) {

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
      subject = "/" + subject;
//      TODO should probably error at this point
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
      wfClient.executeWorkflowPut(SUBJECT, cloudEvent);
    }
  }
}
