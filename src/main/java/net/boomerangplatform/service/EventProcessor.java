package net.boomerangplatform.service;

import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;

public interface EventProcessor {

  void routeEvent(String requestUri, String target, String workflowId, JsonNode payload);

  void routeCloudEvent(String requestUrl, Map<String, Object> headers, JsonNode payload);
}
