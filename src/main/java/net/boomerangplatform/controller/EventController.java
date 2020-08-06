package net.boomerangplatform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
