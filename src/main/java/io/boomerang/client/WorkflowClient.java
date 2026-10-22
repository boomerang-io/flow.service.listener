package io.boomerang.client;

import org.springframework.http.HttpStatusCode;

import io.cloudevents.CloudEvent;

public interface WorkflowClient {

  HttpStatusCode validateWorkflowToken(String workflowId, String token);

  void executeWorkflowPut(CloudEvent jsonPayload);
}
