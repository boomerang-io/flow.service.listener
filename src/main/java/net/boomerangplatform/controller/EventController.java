package net.boomerangplatform.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.boomerangplatform.attributes.TokenAttribute;
import net.boomerangplatform.model.SlackEventPayload;
import net.boomerangplatform.model.WebhookType;
import net.boomerangplatform.service.EventProcessor;

@RestController
@RequestMapping("/listener")
public class EventController {

  @Autowired
  private EventProcessor eventProcessor;

  /**
   * HTTP Webhook accepting Generic, Slack, and Dockerhub subtypes. For Slack and
   * Dockerhub will respond/perform verification challenges.
   * <p>
   * <b>Note:</b> Only partially conformant to the specification.
   * 
   * <h4>Specifications</h4>
   * <ul>
   * <li><a href=
   * "https://github.com/cloudevents/spec/blob/master/http-webhook.md">CloudEvents</a>
   * <li><a href="https://docs.docker.com/docker-hub/webhooks/">Dockerhub</a>
   * <li><a href="https://api.slack.com/events-api">Slack</a>
   * </ul>
   * 
   * <h4>Sample</h4>
   * <code>/webhook?workflowId={workflowId}&type={generic|slack|dockerhub}&access_token={access_token}</code>
   */
  @PostMapping(value = "/webhook", consumes = "application/json; charset=utf-8")
  public ResponseEntity<?> acceptWebhookEvent(HttpServletRequest request, @RequestParam String workflowId,
      @RequestParam WebhookType type, @RequestBody JsonNode payload, @TokenAttribute String token) {

    switch (type) {
      case slack:
        SlackEventPayload response = new SlackEventPayload();
        ObjectMapper mapper = new ObjectMapper();
        SlackEventPayload jsonPayload = mapper.convertValue(payload, SlackEventPayload.class);

        if (jsonPayload != null && "url_verification".equals(jsonPayload.getType())
            && jsonPayload.getChallenge() != null) {
          response.setChallenge(jsonPayload.getChallenge());
          return ResponseEntity.ok(response);
        } else if (payload != null && "event_callback".equals(payload.path("type").asText())) {
          eventProcessor.routeWebhookEvent(token, request.getRequestURL().toString(), "slack", workflowId, payload, null, null);
          return ResponseEntity.ok(HttpStatus.NO_CONTENT);
        } else {
          return ResponseEntity.badRequest().build();
        }
      case dockerhub:
        // TODO: dockerhub callback_url validation
        eventProcessor.routeWebhookEvent(token, request.getRequestURL().toString(), "dockerhub", workflowId, payload, null, null);
        return ResponseEntity.ok(HttpStatus.OK);

      case generic:
        eventProcessor.routeWebhookEvent(token, request.getRequestURL().toString(), "webhook", workflowId, payload, null, null);
        return ResponseEntity.ok(HttpStatus.OK);

      default:
        return ResponseEntity.badRequest().build();
    }
  }

  /**
   * HTTP Webhook specifically for the "Wait For Event" workflow task.
   * 
   * <h4>Sample</h4>
   * <code>/webhook/wfe?workflowId={workflowId}&access_token={access_token}&topic={topic}&workflowActivityId={workflowActivityId}</code>
   */
  @PostMapping(value = "/webhook/wfe", consumes = "application/json; charset=utf-8")
  public ResponseEntity<?> acceptWebhookEvent(HttpServletRequest request, @RequestParam String workflowId,
      @RequestParam String workflowActivityId, @RequestParam String topic, @RequestBody JsonNode payload,
      @TokenAttribute String token) {
    eventProcessor.routeWebhookEvent(token, request.getRequestURL().toString(), "wfe", workflowId, payload, workflowActivityId, topic);
    return ResponseEntity.ok(HttpStatus.OK);
  } 

  /**
   * Accepts any JSON Cloud Event. This will map to the custom trigger but the
   * topic will come from the CloudEvent subject.
   * 
   * @see https://github.com/cloudevents/spec/blob/v1.0/json-format.md
   * @see https://github.com/cloudevents/spec/blob/v1.0/http-protocol-binding.md
   */
  @PutMapping(value = "/event", consumes = "application/cloudevents+json; charset=utf-8")
  public ResponseEntity<HttpStatus> acceptEvent(HttpServletRequest request, @RequestHeader Map<String, Object> headers,
      @RequestBody JsonNode payload, @TokenAttribute String token) {
    eventProcessor.routeCloudEvent(token, request.getRequestURL().toString(), headers, payload);
    return ResponseEntity.ok(HttpStatus.OK);
  }
}