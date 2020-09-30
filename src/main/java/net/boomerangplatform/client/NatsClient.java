package net.boomerangplatform.client;

import net.boomerangplatform.model.Event;

public interface NatsClient {

  void publishMessage(Event event, String action);

  void publishMessage(String subject, String jsonPayload);

}
