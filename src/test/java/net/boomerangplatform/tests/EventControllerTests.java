package net.boomerangplatform.tests;

import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import net.boomerangplatform.Application;
import net.boomerangplatform.MongoConfig;
import net.boomerangplatform.controller.EventController;
import net.boomerangplatform.model.Event;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {Application.class, MongoConfig.class})
@SpringBootTest
@ActiveProfiles("local")
@WithMockUser(roles = {"admin"})
@WithUserDetails("mdroy@us.ibm.com")
public class EventControllerTests {

  @Autowired
  private EventController eventController;

  @Test
  public void testCreatingEvent() throws IOException {

    Event event = new Event();
    event.setDetail("hello", "world");

    ResponseEntity response = eventController.acceptPayload("mail", new Event());

    assertNotNull(response);

  }
}
