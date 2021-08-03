package io.boomerang.service;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.CloudEvent;
import io.cloudevents.v1.AttributesImpl;

public interface EventProcessor {

  ResponseEntity<CloudEvent<AttributesImpl, JsonNode>> routeCloudEvent(CloudEvent<AttributesImpl, JsonNode> cloudEvent, String token, URI uri);

  ResponseEntity<CloudEvent<AttributesImpl, JsonNode>> routeWebhookEvent(String token, String requestUri, String trigger, String workflowId,
      JsonNode payload, String workflowActivityId, String topic, String status);
}
