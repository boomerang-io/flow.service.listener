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
import com.fasterxml.jackson.databind.ObjectMapper;

import net.boomerangplatform.attributes.TokenAttribute;
import net.boomerangplatform.model.SlackEventPayload;
import net.boomerangplatform.service.EventProcessor;

@RestController
@RequestMapping("/listener")
public class EventController {

    @Autowired
    private EventProcessor eventProcessor;

    @PostMapping(value = "/webhook/{workflowId}", consumes = "application/json; charset=utf-8")
    public ResponseEntity<HttpStatus> acceptWebhookEvent(HttpServletRequest request, @PathVariable String workflowId,
            @RequestBody JsonNode payload, @TokenAttribute String token) {
        eventProcessor.routeEvent(token, request.getRequestURL().toString(), "webhook", workflowId, payload);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    /**
     * Accepts the following style of Dockerhub payload
     * 
     * @see https://docs.docker.com/docker-hub/webhooks/
     */
    @PostMapping(value = "/dockerhub/{workflowId}", consumes = "application/json; charset=utf-8")
    public ResponseEntity<HttpStatus> acceptDockerhubEvent(HttpServletRequest request, @PathVariable String workflowId,
            @RequestBody JsonNode payload, @TokenAttribute String token) {
        eventProcessor.routeEvent(token, request.getRequestURL().toString(), "dockerhub", workflowId, payload);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    /**
     * Accepts the custom Slack payload
     */
    @PostMapping(path = "/slack/{workflowId}", consumes = "application/json; charset=utf-8")
    public ResponseEntity<?> submitSlackEvent(HttpServletRequest request, @PathVariable String workflowId,
            @RequestBody JsonNode payload, @TokenAttribute String token) {

        // TODO: Verify token

        SlackEventPayload response = new SlackEventPayload();
        ObjectMapper mapper = new ObjectMapper();
        SlackEventPayload jsonPayload = mapper.convertValue(payload, SlackEventPayload.class);

        if (jsonPayload != null && "url_verification".equals(jsonPayload.getType())
                && jsonPayload.getChallenge() != null) {
            response.setChallenge(jsonPayload.getChallenge());
            return ResponseEntity.ok(response);
        } else if (payload != null && "event_callback".equals(payload.path("type").asText())) {
            eventProcessor.routeEvent(token, request.getRequestURL().toString(), "slack", workflowId, payload);
            return ResponseEntity.ok(HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.badRequest().build();
    }

    /**
     * Accepts any JSON Cloud Event This will map to the custom trigger but the
     * topic will come from the CloudEvent subject.
     * 
     * @see https://github.com/cloudevents/spec/blob/v1.0/json-format.md
     * @see https://github.com/cloudevents/spec/blob/v1.0/http-protocol-binding.md
     */
    @PutMapping(value = "/event", consumes = "application/cloudevents+json; charset=utf-8")
    public ResponseEntity<HttpStatus> acceptEvent(HttpServletRequest request,
            @RequestHeader Map<String, Object> headers, @RequestBody JsonNode payload, @TokenAttribute String token) {
        eventProcessor.routeCloudEvent(token, request.getRequestURL().toString(), headers, payload);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    /**
     * Accepts the Webhook style. Note: Only partially conformant to the
     * specification.
     * 
     * @see https://github.com/cloudevents/spec/blob/master/http-webhook.md
     * @deprecated
     */
    @Deprecated
    @PostMapping(value = "/webhook", consumes = "application/json; charset=utf-8")
    public ResponseEntity<HttpStatus> acceptWebhookEvent(HttpServletRequest request, @RequestBody JsonNode payload,
            @TokenAttribute String token) {
        eventProcessor.routeEvent(token, request.getRequestURL().toString(), "webhook",
                payload.path("workflowId").asText(), payload);

        return ResponseEntity.ok(HttpStatus.OK);
    }
}
