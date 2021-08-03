package io.boomerang.client;

import java.util.concurrent.TimeoutException;

public interface NatsClient {

  void subscribe(String eventId, String subject) throws TimeoutException;

  void publish(String eventId, String jsonPayload);
}
