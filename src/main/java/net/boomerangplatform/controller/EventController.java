package net.boomerangplatform.controller;

import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudevents.CloudEvent;
import io.cloudevents.v1.AttributesImpl;
import net.boomerangplatform.attributes.CloudEventAttribute;
import net.boomerangplatform.attributes.TokenAttribute;
import net.boomerangplatform.model.SlackEventPayload;
import net.boomerangplatform.model.WebhookType;
import net.boomerangplatform.service.EventProcessor;

@RestController
@RequestMapping("/listener")
public class EventController {

  private static final Logger logger = LogManager.getLogger(EventController.class);

  @Autowired
  private EventProcessor eventProcessor;
  
  private static String STATUS_SUCCESS = "success";

  /**
   * HTTP Webhook accepting Generic, Slack Events, and Dockerhub subtypes. For Slack and
   * Dockerhub will respond/perform verification challenges.
   * <p>
   * <b>Note:</b> Only partially conformant to the specification.
   * 
   * <h4>Specifications</h4>
   * <ul>
   * <li><a href=
   * "https://github.com/cloudevents/spec/blob/master/http-webhook.md">CloudEvents</a></li>
   * <li><a href="https://docs.docker.com/docker-hub/webhooks/">Dockerhub</a></li>
   * <li><a href="https://api.slack.com/events-api">Slack Events API</a></li>
   * <li><a href="https://api.slack.com/events">Slack Events</a></li>
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
        if (payload != null) {
          final String slackType = payload.get("type").asText();
  
          if ("url_verification".equals(slackType)) {
            SlackEventPayload response = new SlackEventPayload();
            final String slackChallenge = payload.get("challenge").asText();
            if (slackChallenge != null) {
              response.setChallenge(slackChallenge);
            }
            return ResponseEntity.ok(response);
          } else if (payload != null && ("shortcut".equals(slackType) || "event_callback".equals(slackType))) {
            // Handle Slack Events
            eventProcessor.routeWebhookEvent(token, request.getRequestURL().toString(), "slack", workflowId, payload,
                null, null, STATUS_SUCCESS);
            return ResponseEntity.ok(HttpStatus.OK);
          } else {
            return ResponseEntity.badRequest().build();
          }
        } else {
          return ResponseEntity.badRequest().build();
        }
        
      case dockerhub:
        // TODO: dockerhub callback_url validation
        eventProcessor.routeWebhookEvent(token, request.getRequestURL().toString(), "dockerhub", workflowId, payload,
            null, null, STATUS_SUCCESS);
        return ResponseEntity.ok(HttpStatus.OK);

      case generic:
        eventProcessor.routeWebhookEvent(token, request.getRequestURL().toString(), "webhook", workflowId, payload,
            null, null, STATUS_SUCCESS);
        return ResponseEntity.ok(HttpStatus.OK);

      default:
        return ResponseEntity.badRequest().build();
    }
  }
  
  /**
   * HTTP Webhook accepting Slack Slash and Interactive Commands
   * 
   * <h4>Specifications</h4>
   * <ul>
   * <li><a href="https://api.slack.com/interactivity/handling">Slack Interactivity Handling</a></li>
   * </ul>
   * 
   * <h4>Sample</h4>
   * <code>/webhook?workflowId={workflowId}&type=slack&access_token={access_token}</code>
   * @throws JsonProcessingException 
   * @throws JsonMappingException 
   */
  @PostMapping(value = "/webhook", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
  public ResponseEntity<?> acceptWebhookEvent(HttpServletRequest request, @RequestParam String workflowId,
      @RequestParam WebhookType type, @TokenAttribute String token, @RequestHeader("x-slack-request-timestamp") String timestamp,
      @RequestHeader("x-slack-signature") String signature,
      @RequestParam MultiValueMap<String, String> slackEvent) throws JsonMappingException, JsonProcessingException {

    if (slackEvent.containsKey("payload")) {
      String encodedpayload = slackEvent.get("payload").get(0);
      String decodedPayload = encodedpayload != null ? java.net.URLDecoder.decode(encodedpayload, StandardCharsets.UTF_8) : "";

      ObjectMapper mapper = new ObjectMapper();
      JsonNode payload = mapper.readTree(decodedPayload);
      eventProcessor.routeWebhookEvent(token, request.getRequestURL().toString(), "slack", workflowId, payload,
          null, null, STATUS_SUCCESS);
      return ResponseEntity.ok(HttpStatus.OK);
    } else if (slackEvent.containsKey("command")) {
      
    }
    return ResponseEntity.ok(HttpStatus.UNAUTHORIZED);
  }

  /**
   * HTTP Webhook specifically for the "Wait For Event" workflow task.
   * 
   * <h4>Sample</h4>
   * <code>/webhook/wfe?workflowId={workflowId}&access_token={access_token}&topic={topic}&workflowActivityId={workflowActivityId}</code>
   */
  @PostMapping(value = "/webhook/wfe", consumes = "application/json; charset=utf-8")
  public ResponseEntity<?> acceptWaitForEvent(HttpServletRequest request, @RequestParam String workflowId,
      @RequestParam String workflowActivityId, @RequestParam String topic, @RequestParam(defaultValue = "success") String status,
      @RequestBody JsonNode payload, @TokenAttribute String token) {
    eventProcessor.routeWebhookEvent(token, request.getRequestURL().toString(), "wfe", workflowId, payload,
        workflowActivityId, topic, status);
    return ResponseEntity.ok(HttpStatus.OK);
  }
  
  @GetMapping(value = "/webhook/wfe")
  public ResponseEntity<?> acceptWaitForEvent(HttpServletRequest request, @RequestParam String workflowId,
      @RequestParam String workflowActivityId, @RequestParam String topic, @RequestParam(defaultValue = "success") String status,
      @TokenAttribute String token) {
    eventProcessor.routeWebhookEvent(token, request.getRequestURL().toString(), "wfe", workflowId, null,
        workflowActivityId, topic, status);
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
  public ResponseEntity<HttpStatus> acceptEvent(@CloudEventAttribute CloudEvent<AttributesImpl, JsonNode> cloudEvent,
      @TokenAttribute String token) {
    eventProcessor.routeCloudEvent(cloudEvent, token,
        ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri());
    return ResponseEntity.ok(HttpStatus.OK);
  }
}
