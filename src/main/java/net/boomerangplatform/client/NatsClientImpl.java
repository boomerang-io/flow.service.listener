package net.boomerangplatform.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import io.nats.streaming.Message;
import io.nats.streaming.MessageHandler;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import io.nats.streaming.Subscription;
import io.nats.streaming.SubscriptionOptions;

@Service
public class NatsClientImpl implements NatsClient {

  private static final Logger logger = Logger.getLogger(NatsClientImpl.class.getName());

  @Value("${eventing.nats.url}")
  private String natsUrl;
  
  @Value("${eventing.nats.cluster}")
  private String natsCluster;

  @Override
  /* 
   * Publishes CloudEvent payload to NATS
   * https://github.com/cloudevents/spec/blob/master/nats-protocol-binding.md
   */
  public void publish(String eventId, String subject, String jsonPayload) {
    try {
      StreamingConnectionFactory cf = getStreamingConnectionFactory("listener");

      StreamingConnection sc = cf.createConnection();
      sc.publish(subject, jsonPayload.getBytes(StandardCharsets.UTF_8));

    } catch (IOException e) {
      logger.log(Level.SEVERE, "Error: ", e);
    } catch (InterruptedException | TimeoutException ex) {
      Thread.currentThread().interrupt();
      logger.log(Level.SEVERE, "Error: ", ex);
    } 
    
//    Do we need a finally with sc.close()

  }

  private StreamingConnectionFactory getStreamingConnectionFactory(String clientId) {
    logger.info("Initializng subscriptions to NATS");

    int random = (int) (Math.random() * 10000 + 1); // NOSONAR
    
    Options cfOptions = new Options.Builder().natsUrl(natsUrl).clusterId(natsCluster).clientId(clientId + "-" +random).build();
    StreamingConnectionFactory cf =
        new StreamingConnectionFactory(cfOptions);
    return cf;
  }
  
  @Override
  /* 
   * Subscribes to CloudEvent payload for a response
   * https://github.com/cloudevents/spec/blob/master/nats-protocol-binding.md
   */
public void subscribe(String eventId, String subject) throws TimeoutException {
//
//    logger.info("Initializng subscriptions to NATS");
//
//    int random = (int) (Math.random() * 10000 + 1); // NOSONAR
//
//    Options cfOptions = new Options.Builder().natsUrl(natsUrl).clusterId(natsCluster).clientId("flow-workflow-" + random).build();
//    StreamingConnectionFactory cf = new StreamingConnectionFactory(cfOptions);
//    
//    try {
//      this.streamingConnection = cf.createConnection();
//
//      Subscription subscription =
//          streamingConnection.subscribe(SUBJECT, QUEUE, new MessageHandler() { // NOSONAR
//            @Override
//            public void onMessage(Message m) {
//              
//              eventProcessor.processMessage(new String(m.getData()));
//            }
//          }, new SubscriptionOptions.Builder().durableName("durable").build());
//    } catch (IOException ex) {
//      logger.error(ex);
//    } catch (InterruptedException ex) {
//      Thread.currentThread().interrupt();
//    }
////    TODO do we close connection and subscription?
}
}
