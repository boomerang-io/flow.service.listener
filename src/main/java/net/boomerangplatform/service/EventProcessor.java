package net.boomerangplatform.service;

import java.util.Map;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;

public interface EventProcessor {

  HttpStatus routeCloudEvent(String token, String requestUrl, Map<String, Object> headers, JsonNode payload);

  HttpStatus routeWebhookEvent(String token, String string, String trigger, String workflowId,
      JsonNode payload, String workflowActivityId, String topic);
}
