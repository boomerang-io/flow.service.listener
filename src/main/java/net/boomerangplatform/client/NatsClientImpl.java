package net.boomerangplatform.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import net.boomerangplatform.model.Event;
import net.boomerangplatform.model.EventPayload;

@Service
public class NatsClientImpl implements NatsClient {

  private static final Logger LOGGER = Logger.getLogger(NatsClientImpl.class.getName());

  @Value("${actionlistener.nats.url}")
  private String natsUrl;

  @Value("${actionlistener.nats.cluster}")
  private String natsCluster;

  @Override
  public void publishMessage(Event event, String action) {
    try {
      StreamingConnectionFactory cf =
          new StreamingConnectionFactory(natsCluster, "action-listener");
      cf.setNatsUrl(natsUrl);

      EventPayload eventPayload = new EventPayload();
      eventPayload.setEvent(event);
      eventPayload.setDestination(action);

      try (StreamingConnection sc = cf.createConnection()) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        String json =
            objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(eventPayload);
        sc.publish(action, json.getBytes(StandardCharsets.UTF_8));
      }

    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error: ", e);
    } catch (InterruptedException | TimeoutException ex) {
      Thread.currentThread().interrupt();
      LOGGER.log(Level.SEVERE, "Error: ", ex);
    }

  }
}
