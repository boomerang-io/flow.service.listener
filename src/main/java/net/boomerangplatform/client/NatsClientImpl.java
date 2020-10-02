package net.boomerangplatform.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;

@Service
public class NatsClientImpl implements NatsClient {

  private static final Logger LOGGER = Logger.getLogger(NatsClientImpl.class.getName());

  @Value("${eventing.nats.url}")
  private String natsUrl;
  
  @Value("${eventing.nats.cluster}")
  private String natsCluster;

  @Override
  /* 
   * Publishes CloudEvent payload to NATS
   * https://github.com/cloudevents/spec/blob/master/nats-protocol-binding.md
   */
  public void publish(String subject, String jsonPayload) {
    try {
      StreamingConnectionFactory cf = getStreamingConnectionFactory();

      StreamingConnection sc = cf.createConnection();
      sc.publish(subject, jsonPayload.getBytes(StandardCharsets.UTF_8));

    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error: ", e);
    } catch (InterruptedException | TimeoutException ex) {
      Thread.currentThread().interrupt();
      LOGGER.log(Level.SEVERE, "Error: ", ex);
    } 
    
//    Do we need a finally with sc.close()

  }

  private StreamingConnectionFactory getStreamingConnectionFactory() {
    LOGGER.info("Initializng subscriptions to NATS");

    int random = (int) (Math.random() * 10000 + 1); // NOSONAR
    
    Options cfOptions = new Options.Builder().natsUrl(natsUrl).clusterId(natsCluster).clientId("listener-"+random).build();
    StreamingConnectionFactory cf =
        new StreamingConnectionFactory(cfOptions);
    return cf;
  }
}
