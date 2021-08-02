package net.boomerangplatform.client;

import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.JsonNode;
import io.cloudevents.v1.CloudEventImpl;

public interface WorkflowClient {

  HttpStatus validateWorkflowToken(String workflowId, String token);

  void executeWorkflowPut(CloudEventImpl<JsonNode> jsonPayload);
}
