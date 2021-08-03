package io.boomerang.service;

import java.net.URI;

import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.cloudevents.CloudEvent;
import io.cloudevents.v1.AttributesImpl;

public interface EventProcessor {

  ResponseEntity<CloudEvent<AttributesImpl, JsonNode>> routeCloudEvent(CloudEvent<AttributesImpl, JsonNode> cloudEvent, String token, URI uri);

  HttpStatus routeWebhookEvent(String token, String requestUri, String trigger, String workflowId,
      JsonNode payload, String workflowActivityId, String topic, String status);

  HttpStatus validateCloudEvent(CloudEvent<AttributesImpl, JsonNode> cloudEvent, String token,
      URI uri);
}
