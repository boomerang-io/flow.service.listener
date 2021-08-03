package io.boomerang.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ExtendWith(SpringExtension.class)
class EventServiceTests {

  @Value("${workflow.service.url.execute}")
  private String executeWorkflowUrl;

  @Value("${workflow.service.url.validateToken}")
  private String validateTokenWorkflowUrl;

  @Autowired
  @Qualifier("internalRestTemplate")
  RestTemplate restTemplate;

  private MockRestServiceServer server;

  @BeforeEach
  public void init() {
    this.server = MockRestServiceServer.createServer(restTemplate);
  }

  @Test
  void test() {

  }


}
