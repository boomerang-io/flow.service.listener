package net.boomerangplatform.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.nats.client.ConnectionListener;
import io.nats.streaming.AckHandler;
import io.nats.streaming.Options;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;

@Service
public class NatsClientImpl implements NatsClient {

  private static final Logger logger = LogManager.getLogger(NatsClientImpl.class);

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
      StreamingConnectionFactory cf = getStreamingConnectionFactory("flow-listener");

      StreamingConnection sc = cf.createConnection();
      AckHandler ackHandler = new AckHandler() {
        public void onAck(String guid, Exception err) {
          if (err != null) {
            System.err.printf("Error publishing msg id %s: %s\n", guid, err.getMessage());
          } else {
            System.out.printf("Received ack for msg id %s\n", guid);
          }
        }
      };
      String guid = sc.publish(subject, jsonPayload.getBytes(StandardCharsets.UTF_8), ackHandler);

    } catch (IOException e) {
      logger.error(e.toString());
    } catch (InterruptedException | TimeoutException ex) {
      Thread.currentThread().interrupt();
      logger.error(ex.toString());
    }

    // Do we need a finally with sc.close()

  }

  private StreamingConnectionFactory getStreamingConnectionFactory(String clientId) {
    int random = (int) (Math.random() * 10000 + 1); // NOSONAR
    
    logger.info("Initializng subscriptions to NATS with URL: " + natsUrl + ", Cluster: " + natsCluster + ", Client ID: " + clientId + "-" +random);
    
    Options cfOptions = new Options.Builder().natsUrl(natsUrl).clusterId(natsCluster).clientId(clientId + "-" +random)
        .connectionListener((conn, type) -> {
          if (type == ConnectionListener.Events.CONNECTED) {
            logger.info("Connected to Nats Server");
          } else if (type == ConnectionListener.Events.RECONNECTED) {
            logger.info("Reconnected to Nats Server");
          } else if (type == ConnectionListener.Events.DISCONNECTED) {
            logger.error("Disconnected to Nats Server, reconnect attempt in seconds");
          } else if (type == ConnectionListener.Events.CLOSED) {
            logger.info("Closed connection with Nats Server");
          }
      }).build();
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
