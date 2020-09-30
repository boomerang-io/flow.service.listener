package net.boomerangplatform.service;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.json.Json;
import io.cloudevents.v1.CloudEventBuilder;
import io.cloudevents.v1.CloudEventImpl;
import net.boomerangplatform.client.NatsClient;
import net.boomerangplatform.model.Event;

@Service
public class EventProcessorImpl implements EventProcessor {

  protected static final String TYPE_DNS = "io.boomerang.eventing.";
  
  protected static final String SUBJECT_PREFIX = "flow.";

  private static final Logger LOGGER = LogManager.getLogger(EventProcessorImpl.class);

  @Autowired
  private NatsClient natsClient;

  @Override
  public void routeEvent(String action, Event event) {
    natsClient.publishMessage(event, action);
  }

  @Override
  public void routeCloudEvent(String requestUri, String target, String workflowId, JsonNode payload) {
    final String eventId = UUID.randomUUID().toString();
    final String eventType = TYPE_DNS + target;
    final String subject = SUBJECT_PREFIX + workflowId;
    final URI uri = URI.create(requestUri);
        
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
    LOGGER.info("CloudEvent Object - " + jsonPayload);
    natsClient.publishMessage(subject, jsonPayload);
  }
}
