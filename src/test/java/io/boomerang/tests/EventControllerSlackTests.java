package io.boomerang.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@ActiveProfiles("local")
@AutoConfigureMockMvc
@SpringBootTest
@ExtendWith(SpringExtension.class)
class EventControllerSlackTests {

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
    
    server.expect(manyTimes(), requestTo(validateTokenWorkflowUrl.replace("{workflowId}", workflowId)))
    .andExpect(method(HttpMethod.POST))
    .andRespond(withSuccess());
    
    server.expect(manyTimes(), requestTo(executeWorkflowUrl))
    .andExpect(method(HttpMethod.PUT))
    .andRespond(withSuccess());
  }
  
//  @Test
//  public void testSlackEvent() throws IOException {
//    
//    String payloadAsString = TestUtil.getMockFile("json/slack-event-payload.json");
//    ObjectMapper mapper = new ObjectMapper();
//    JsonNode payload = mapper.readTree(payloadAsString);
//    
//    System.out.println("v() - Request Payload: " + payload.toPrettyString());
//
//    try {
//      MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/listener/webhook")
//          .param("workflowId", workflowId).param("type", "slack")
//          .param("access_token", "A26ABB1F850BA80A625E4A9878795BC17EAD1A8FB8F8232B96003036402F6C66")
//          .content(payloadAsString).contentType(MediaType.APPLICATION_JSON)).andReturn();
//      
//      System.out.println("testSlackShortcutType() - Response: " + result.getResponse().getContentAsString());
//    } catch (Exception e) {
//      e.printStackTrace();
//      Assert.fail("Unexpected Exception");
//    }
//  }
  
  @Test
  public void testSlackShortcutType() throws IOException {
    
    String encodedpayload = "%7B%22type%22%3A%22shortcut%22%2C%22token%22%3A%22H3JpI2osywM0TsERaT3uBCOQ%22%2C%22action_ts%22%3A%221622696074.997119%22%2C%22team%22%3A%7B%22id%22%3A%22T27TLPNS1%22%2C%22domain%22%3A%22gbs-hcs%22%2C%22enterprise_id%22%3A%22E27SFGS2W%22%2C%22enterprise_name%22%3A%22IBM%22%7D%2C%22user%22%3A%7B%22id%22%3A%22W3FECR56F%22%2C%22username%22%3A%22twlawrie%22%2C%22team_id%22%3A%22T27TLPNS1%22%7D%2C%22is_enterprise_install%22%3Afalse%2C%22enterprise%22%3A%7B%22id%22%3A%22E27SFGS2W%22%2C%22name%22%3A%22IBM%22%7D%2C%22callback_id%22%3A%22esssc-create-issue%22%2C%22trigger_id%22%3A%222128550766389.75938804885.a50b7b03b18fe1fe3353d86cd4b0b36c%22%7D";
    String decodedPayload = encodedpayload != null ? java.net.URLDecoder.decode(encodedpayload, StandardCharsets.UTF_8) : "";
    String decodedPayloadAsJson = TestUtil.getMockFile("json/slack-shortcut-decoded.json");
    
    assertEquals(decodedPayload, decodedPayloadAsJson);
    
    ObjectMapper mapper = new ObjectMapper();
    JsonNode payload = mapper.readTree(decodedPayload);
    
    System.out.println("testSlackShortcutType() - Request Payload: " + payload.toPrettyString());
    
    MultiValueMap<String, String> payloadMap = new LinkedMultiValueMap<String, String>();
    payloadMap.add("payload", encodedpayload);

    try {
        MvcResult result = mockMvc.perform(
            MockMvcRequestBuilders.post("/listener/webhook")
          .param("workflowId", workflowId).param("type", "slack")
          .param("access_token", "A26ABB1F850BA80A625E4A9878795BC17EAD1A8FB8F8232B96003036402F6C66")
          .header("x-slack-signature", "v0=435cb53bfc0eaa28ca101609ac1f8cd23abaf603762af18b1883600cf9c2dba8")
          .header("x-slack-request-timestamp", "1622696075")
          .params(payloadMap)
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)).andReturn();
      
      System.out.println("testSlackShortcutType() - Response: " + result.getResponse().getContentAsString());
      
      assertEquals(result.getResponse().getStatus(), 200);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected Exception");
    }
  }
  
  @Test
  void testSlackInvalidType() throws IOException {
    
    String payloadAsString = TestUtil.getMockFile("json/slack-invalidtype-payload.json");
    ObjectMapper mapper = new ObjectMapper();
    JsonNode payload = mapper.readTree(payloadAsString);
    
    System.out.println("testSlackInvalidType() - Request Payload: " + payload.toPrettyString());

    try {
      MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/listener/webhook")
          .param("workflowId", workflowId).param("type", "slack")
          .param("access_token", "A26ABB1F850BA80A625E4A9878795BC17EAD1A8FB8F8232B96003036402F6C66")
          .content(payloadAsString).contentType(MediaType.APPLICATION_JSON)).andReturn();
      
      System.out.println("testSlackInvalidType() - Status: " + result.getResponse().getStatus());
      
      assertEquals(result.getResponse().getStatus(), 400);
    } catch (Exception e) {
      e.printStackTrace();
      fail("Unexpected Exception");
    }
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
