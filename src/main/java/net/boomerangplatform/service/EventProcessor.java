package net.boomerangplatform.service;

import com.fasterxml.jackson.databind.JsonNode;
import net.boomerangplatform.model.Event;

public interface EventProcessor {

  void routeEvent(String target, Event event);

  void routeCloudEvent(String requestUri, String target, String workflowId, JsonNode payload);
}
