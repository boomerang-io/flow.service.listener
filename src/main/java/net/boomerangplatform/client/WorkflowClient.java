package net.boomerangplatform.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.v1.CloudEventImpl;

public interface WorkflowClient {

  void executeWorkflowPut(String subject, CloudEventImpl<JsonNode> jsonPayload);

  Boolean validateTriggerToken(String workflowId, String trigger, String token);
}
