package net.boomerangplatform.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
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
  
  static private int CLIENT_ID_UNIQUE_INT = (int) (Math.random() * 10000 + 1); // NOSONAR 
  
  static private String CLIENT_ID_PREFIX = "listener";

  /**
   * Publishes CloudEvent payload to NATS.
   * 
   * @see https://github.com/cloudevents/spec/blob/master/nats-protocol-binding.md
   */
  @Override
  public void publish(String eventId, String subject, String jsonPayload) {
    try {
      StreamingConnectionFactory connectionFactory = getStreamingConnectionFactory();

      // `StreamingConnection` extends `AutoCloseable`, the connection closes
      // automatically after try statement
      try (StreamingConnection streamingConnection = connectionFactory.createConnection()) {
        streamingConnection.publish(subject, jsonPayload.getBytes(StandardCharsets.UTF_8));
      }

    } catch (IOException exception) {
      logger.error(exception.toString());
    } catch (InterruptedException | TimeoutException exception) {
      Thread.currentThread().interrupt();
      logger.error(exception.toString());
    } catch (Exception exception) {
      logger.error(exception.toString());
    }
    
  }

  /**
   * Subscribes to CloudEvent payload for a response.
   * 
   * @throws InterruptedException
   * @throws IOException
   * 
   * @see https://github.com/cloudevents/spec/blob/master/nats-protocol-binding.md
   */
  @Override
  public void subscribe(String eventId, String subject) {

    // @formatter:off
    // logger.info("Initializng subscriptions to NATS");
    //
    // int random = (int) (Math.random() * 10000 + 1); // NOSONAR
    //
    // Options cfOptions = new Options.Builder().natsUrl(natsUrl).clusterId(natsCluster)
    //         .clientId("flow-workflow-" + random).build();
    // StreamingConnectionFactory connectionFactory = new StreamingConnectionFactory(cfOptions);
    //
    // try (StreamingConnection streamingConnection = connectionFactory.createConnection()) {
    //
    //     streamingConnection.subscribe("SUBJECT", "QUEUE", new MessageHandler() {
    //
    //         @Override
    //         public void onMessage(Message msg) {
    //             eventProcessor.processMessage(new String(msg.getData()));
    //         }
    //     });
    // } catch (IOException exception) {
    //     logger.log(Level.SEVERE, "Error: ", exception);
    // } catch (InterruptedException | TimeoutException exception) {
    //     Thread.currentThread().interrupt();
    // }
    // // TODO do we close connection and subscription?
    // @formatter:on
  }

  /**
   * @category Helper method.
   */
  private StreamingConnectionFactory getStreamingConnectionFactory() {
    logger.info("Initializng subscriptions to NATS with Client ID: " + CLIENT_ID_PREFIX + "-" + CLIENT_ID_UNIQUE_INT);

    Options options = new Options.Builder().natsUrl(natsUrl).clusterId(natsCluster).clientId(CLIENT_ID_PREFIX + "-" + CLIENT_ID_UNIQUE_INT)
        .build();
    return new StreamingConnectionFactory(options);
  }
}
