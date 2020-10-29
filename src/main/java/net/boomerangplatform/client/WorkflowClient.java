package net.boomerangplatform.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.v1.CloudEventImpl;

public interface WorkflowClient {

  Boolean validateWorkflowToken(String workflowId, String token);

  void executeWorkflowPut(CloudEventImpl<JsonNode> jsonPayload);
}
