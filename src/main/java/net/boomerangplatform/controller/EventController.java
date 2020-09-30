package net.boomerangplatform.controller;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import net.boomerangplatform.model.Event;
import net.boomerangplatform.service.EventProcessor;

@RestController
@RequestMapping("/listener")
public class EventController {

  @Autowired
  private EventProcessor eventProcessor;

  @PostMapping(value = "/payload")
  public ResponseEntity<HttpStatus> acceptPayload(
      @RequestHeader(value = "X-Core-Event") String coreEvent, @RequestBody Event event) {

    eventProcessor.routeEvent(coreEvent, event);

    return ResponseEntity.ok(HttpStatus.OK);
  }
  
  @PostMapping(value = "/dockerhub/{workflowId}", consumes = "application/json; charset=utf-8")
  public ResponseEntity<HttpStatus> acceptDockerhubEvent(HttpServletRequest request, @PathVariable String workflowId, @RequestBody JsonNode payload) {
    eventProcessor.routeCloudEvent(request.getRequestURL().toString(), "dockerhub", workflowId, payload);

    return ResponseEntity.ok(HttpStatus.OK);
  }
}
