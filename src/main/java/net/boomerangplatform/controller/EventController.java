package net.boomerangplatform.controller;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import net.boomerangplatform.service.EventProcessor;

@RestController
@RequestMapping("/listener")
public class EventController {

  @Autowired
  private EventProcessor eventProcessor;
  
  /*
   * Accepts the Webhook style. 
   * Note: Only partially conformant to the specification.
   * https://github.com/cloudevents/spec/blob/master/http-webhook.md
   */
  @Deprecated
  @PostMapping(value = "/webhook", consumes = "application/json; charset=utf-8")
  public ResponseEntity<HttpStatus> acceptWebhookEvent(HttpServletRequest request, @RequestBody JsonNode payload) {
    eventProcessor.routeEvent(request.getRequestURL().toString(), "webhook", payload.path("workflowId").asText(), payload);

    return ResponseEntity.ok(HttpStatus.OK);
  }
  
  @PostMapping(value = "/webhook/{workflowId}", consumes = "application/json; charset=utf-8")
  public ResponseEntity<HttpStatus> acceptWebhookEvent(HttpServletRequest request, @PathVariable String workflowId, @RequestBody JsonNode payload) {
    eventProcessor.routeEvent(request.getRequestURL().toString(), "webhook", workflowId, payload);

    return ResponseEntity.ok(HttpStatus.OK);
  }
  
  /*
   * Accepts the following style of Dockerhub payload
   * https://docs.docker.com/docker-hub/webhooks/
   */
  @PostMapping(value = "/dockerhub/{workflowId}", consumes = "application/json; charset=utf-8")
  public ResponseEntity<HttpStatus> acceptDockerhubEvent(HttpServletRequest request, @PathVariable String workflowId, @RequestBody JsonNode payload) {
    eventProcessor.routeEvent(request.getRequestURL().toString(), "dockerhub", workflowId, payload);

    return ResponseEntity.ok(HttpStatus.OK);
  }
  
  @PutMapping(value = "/event", consumes = "application/cloudevents+json; charset=utf-8")
  public ResponseEntity<HttpStatus> acceptWebhookEvent(HttpServletRequest request, @RequestHeader Map<String, Object> headers, @RequestBody JsonNode payload) {
    eventProcessor.routeCloudEvent(request.getRequestURL().toString(), headers, payload);

    return ResponseEntity.ok(HttpStatus.OK);
  }
}
