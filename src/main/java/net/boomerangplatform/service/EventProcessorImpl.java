package net.boomerangplatform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import net.boomerangplatform.client.NatsClient;
import net.boomerangplatform.model.Event;

@Service
public class EventProcessorImpl implements EventProcessor {

  @Autowired
  private NatsClient natsClient;


  @Override
  public void routeEvent(String action, Event event) {
    natsClient.publishMessage(event, action);
  }

}
