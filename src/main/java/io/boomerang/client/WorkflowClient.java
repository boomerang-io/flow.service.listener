package io.boomerang.client;

import org.springframework.http.HttpStatusCode;

import com.fasterxml.jackson.databind.JsonNode;

import io.cloudevents.v1.CloudEventImpl;

public interface WorkflowClient {

  HttpStatusCode validateWorkflowToken(String workflowId, String token);

  void executeWorkflowPut(CloudEventImpl<JsonNode> jsonPayload);
}
