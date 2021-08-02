package net.boomerangplatform.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withUnauthorizedRequest;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("local")
@AutoConfigureMockMvc
@SpringBootTest
@ExtendWith(SpringExtension.class)
class EventControllerUnauthorizedTests {

  @Value("${workflow.service.url.execute}")
  private String executeWorkflowUrl;

  @Value("${workflow.service.url.validateToken}")
  private String validateTokenWorkflowUrl;

  @Autowired
  @Qualifier("internalRestTemplate")
  RestTemplate restTemplate;

  private MockRestServiceServer server;
  
  @Autowired
  private MockMvc mockMvc;
  
  private String workflowId = "60b5d4a91817f67ac3c44bd1";
  
  @BeforeEach
  public void init() {
     this.server = MockRestServiceServer.createServer(restTemplate);
    server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    
    server.expect(once(), requestTo(validateTokenWorkflowUrl.replace("{workflowId}", workflowId)))
    .andExpect(method(HttpMethod.POST))
    .andRespond(withUnauthorizedRequest());
  }
  
  @Test
  void testEventUnauthorized() throws IOException, URISyntaxException {
    
//    String eventId = UUID.randomUUID().toString();
//    String eventType = "io.boomerang.eventing.custom";
//    URI uri = new URI("/internal");
//    String subject = "/5f74d0293979cd04c7f8afa1";
//    CustomAttributeExtension statusCAE = new CustomAttributeExtension("status", "success");
    String content = "{" //
        + "\"id\":\"1234\"," //
        + "\"specversion\":\"1.0\"," //
        + "\"type\":\"io.boomerang.eventing.custom\"," //
        + "\"subject\":\"/5f74d0293979cd04c7f8afa1\"," //
        + "\"source\":\"/internal\"," //
        + "\"data\":{\"value\":\"test\"}}";
    
//    final CloudEventImpl<JsonNode> forwardedCloudEvent = CloudEventBuilder.<JsonNode>builder().withType(eventType).withExtension(statusCAE)
//        .withId(eventId).withSource(uri).withSubject(subject)
//        .withTime(ZonedDateTime.now()).build();
    
    System.out.println("testEventUnauthorized() - Request Payload: " + content);

    try {
      MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/listener/event")
          .header("Authorization", "shouldnotwork")
          .header("Content-Type", "application/cloudevents+json")
          .content(content)).andReturn();
      
      System.out.println("testEventUnauthorized() - Status: " + result.getResponse().getStatus());
      
      assertEquals(result.getResponse().getStatus(), 400);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected Exception");
    }
  }
  

}
