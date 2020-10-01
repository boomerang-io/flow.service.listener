package net.boomerangplatform.client;

public interface NatsClient {

  void publishMessage(String subject, String jsonPayload);

}
