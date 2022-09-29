package io.boomerang.client;

import org.springframework.http.HttpStatus;
import io.cloudevents.CloudEvent;

public interface WorkflowClient {

  HttpStatus validateWorkflowToken(String workflowId, String token);

  void executeWorkflowPut(CloudEvent jsonPayload);
}
