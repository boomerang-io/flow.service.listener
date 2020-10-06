package net.boomerangplatform.tests;

import java.io.IOException;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EventControllerTests {

    @Test
    public void testSubjectToWorkflowIdAndTopic() throws IOException {
        String subject = "/5f74d0293979cd04c7f8afa1/Custom Topic";
        String[] splitArr = subject.split("/");
        System.out.println(splitArr.length);

        for (Integer i = 0; i < splitArr.length; i++) {
            System.out.println(splitArr[i]);
        }
    }
}
