package net.boomerangplatform.service;

import java.util.Map;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;

public interface EventProcessor {

  HttpStatus routeWebhookEvent(String token, String requestUri, String target, String workflowId, JsonNode payload);

  HttpStatus routeCloudEvent(String token, String requestUrl, Map<String, Object> headers, JsonNode payload);
}
