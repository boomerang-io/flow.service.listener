package net.boomerangplatform.client;

public interface NatsClient {

  void publish(String subject, String jsonPayload);

}
