package io.boomerang.config;

import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.boomerang.eventing.nats.ConnectionPrimer;
import io.boomerang.eventing.nats.jetstream.PubOnlyConfiguration;
import io.boomerang.eventing.nats.jetstream.PubOnlyTunnel;
import io.boomerang.eventing.nats.jetstream.PubTransmitter;
import io.nats.client.Options;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;

@Configuration
public class EventingConfig {

  private ConnectionPrimer connectionPrimer;

  @Value("#{'${eventing.nats.server.urls}'.split(',')}")
  private List<String> serverUrls;

  @Value("${eventing.nats.server.reconnect-wait-time:PT10S}")
  private Duration serverReconnectWaitTime;

  @Value("${eventing.nats.server.reconnect-max-attempts:-1}")
  private Integer serverReconnectMaxAttempts;

  @Value("${eventing.jetstream.stream.name}")
  private String jetstreamStreamName;

  @Value("${eventing.jetstream.stream.storage-type}")
  private StorageType jetstreamStreamStorageType;

  @Value("${eventing.jetstream.stream.subject}")
  private String jetstreamStreamSubject;

  @Bean
  @ConditionalOnProperty(value = "eventing.enabled", havingValue = "true", matchIfMissing = false)
  public PubOnlyTunnel pubOnlyTunnel() {

    // @formatter:off
    StreamConfiguration streamConfiguration = new StreamConfiguration.Builder()
        .name(jetstreamStreamName)
        .storageType(jetstreamStreamStorageType)
        .subjects(jetstreamStreamSubject)
        .build();
    PubOnlyConfiguration pubOnlyConfiguration = new PubOnlyConfiguration.Builder()
        .automaticallyCreateStream(true)
        .build();
    // @formatter:on

    return new PubTransmitter(getConnectionPrimer(), streamConfiguration, pubOnlyConfiguration);
  }

  private ConnectionPrimer getConnectionPrimer() {

    if (connectionPrimer == null) {

      // @formatter:off
      Options.Builder optionsBuilder = new Options.Builder()
          .servers(serverUrls.toArray(new String[0]))
          .reconnectWait(serverReconnectWaitTime)
          .maxReconnects(serverReconnectMaxAttempts);
      // @formatter:on

      connectionPrimer = new ConnectionPrimer(optionsBuilder);
    }

    return connectionPrimer;
  }
}
