package net.boomerangplatform.tests;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import net.boomerangplatform.service.EventProcessor;

@SpringBootTest
public class EventControllerTests {

  @Value("${workflow.service.url.execute}")
  private String executeWorkflowUrl;

  @Value("${workflow.service.url.validateToken}")
  private String validateTokenWorkflowUrl;

  @Autowired
  RestTemplate restTemplate;

  private MockRestServiceServer server;

  @Autowired
  private EventProcessor eventProcessor;
  
  @Before
  public void init() {
     this.server = MockRestServiceServer.createServer(restTemplate);
  }

  @Test
  public void testSubjectToWorkflowIdAndTopic() throws IOException {
    String subject = "/5f74d0293979cd04c7f8afa1/Custom Topic";
    String[] splitArr = subject.split("/");
    System.out.println(splitArr.length);

    for (Integer i = 0; i < splitArr.length; i++) {
      System.out.println(splitArr[i]);
    }
  }
  
  @Test
  public void testValidateToken() throws IOException {

    server = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
    
    String workflowId = "5f74d0293979cd04c7f8afa1";

    this.server.expect(requestTo(validateTokenWorkflowUrl.replace("{workflowId}", workflowId))).andRespond(
        withSuccess());
    
//    eventProcessor.routeWebhookEvent(token, requestUri, target, workflowId, payload);
  }
}
