package net.boomerangplatform.tests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.boomerangplatform.controller.EventController;

@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@ActiveProfiles("local")
@SpringBootTest
public class EventControllerTests {

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
  
  @InjectMocks
  EventController eventController;
  
  private String workflowId = "60b5d4a91817f67ac3c44bd1";
  
  @Before
  public void init() {
     this.server = MockRestServiceServer.createServer(restTemplate);
    server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    
    server.expect(manyTimes(), requestTo(validateTokenWorkflowUrl.replace("{workflowId}", workflowId)))
    .andExpect(method(HttpMethod.POST))
    .andRespond(withSuccess());
    
    server.expect(manyTimes(), requestTo(executeWorkflowUrl))
    .andExpect(method(HttpMethod.PUT))
    .andRespond(withSuccess());
  }
  
  @Test
  public void testSlackShortcutType() throws IOException {
    
    String payloadAsString = TestUtil.getMockFile("json/event-slack-shortcut-payload.json");
    ObjectMapper mapper = new ObjectMapper();
    JsonNode payload = mapper.readTree(payloadAsString);
    
    System.out.println("v() - Request Payload: " + payload.toPrettyString());

    try {
      MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/listener/webhook")
          .param("workflowId", workflowId).param("type", "slack")
          .param("access_token", "A26ABB1F850BA80A625E4A9878795BC17EAD1A8FB8F8232B96003036402F6C66")
          .content(payloadAsString).contentType(MediaType.APPLICATION_JSON)).andReturn();
      
      System.out.println("testSlackShortcutType() - Response: " + result.getResponse().getContentAsString());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("Unexpected Exception");
    }
  }
  
  @Test
  public void testSlackInvalidType() throws IOException {
    
    String payloadAsString = TestUtil.getMockFile("json/event-slack-invalidtype-payload.json");
    ObjectMapper mapper = new ObjectMapper();
    JsonNode payload = mapper.readTree(payloadAsString);
    
    System.out.println("testSlackInvalidType() - Request Payload: " + payload.toPrettyString());

    try {
      MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/listener/webhook")
          .param("workflowId", workflowId).param("type", "slack")
          .param("access_token", "A26ABB1F850BA80A625E4A9878795BC17EAD1A8FB8F8232B96003036402F6C66")
          .content(payloadAsString).contentType(MediaType.APPLICATION_JSON)).andReturn();
      
      Assert.assertEquals(result.getResponse().getStatus(), 400);
      
      System.out.println("testSlackInvalidType() - Status: " + result.getResponse().getStatus());
    } catch (Exception e) {
      e.printStackTrace();
      Assert.fail("Unexpected Exception");
    }
  }
  

}
