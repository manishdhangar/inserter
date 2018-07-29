import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import data.creator.CreateTestJson;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CreateTestJson.class)
public class CreateTestJsonTest {

	@Autowired
	CreateTestJson jg;

	@Test
	public void jsonGeneratorTest() throws Exception {
		jg.createJson(null);
	}

}
