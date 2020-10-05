package net.boomerangplatform.tests;

import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import net.boomerangplatform.Application;
import net.boomerangplatform.MongoConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class, MongoConfig.class})
@SpringBootTest
@ActiveProfiles("local")
@WithMockUser(roles = {"admin"})
@WithUserDetails("mdroy@us.ibm.com")
public class EventControllerTests {

  @Test
  public void testSubjectToWorkflowIdAndTopic() throws IOException {
    String subject = "/5f74d0293979cd04c7f8afa1/Custom Topic";
    String[] splitArr = subject.split("/");
    System.out.println(splitArr.length);
    
    for (Integer i=0; i < splitArr.length; i++) {
      System.out.println(splitArr[i]);
    }
  }
}
