package io.boomerang.service;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.CloudEvent;

public interface EventProcessor {

  ResponseEntity<CloudEvent> routeCloudEvent(CloudEvent cloudEvent, String token, URI uri);

  ResponseEntity<CloudEvent> routeWebhookEvent(String token, String requestUri, String trigger,
      String workflowId,
      JsonNode payload, String workflowActivityId, String topic, String status);
}
