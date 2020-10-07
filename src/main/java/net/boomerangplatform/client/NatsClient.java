package net.boomerangplatform.client;

import java.util.concurrent.TimeoutException;

public interface NatsClient {

  void publish(String eventId, String subject, String jsonPayload);

  void subscribe(String eventId, String subject) throws TimeoutException;
}
