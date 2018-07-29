import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import eptf.solver.Fast;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = Fast.class)
public class FastTest {

	@Autowired
	Fast fs;

	@Test
	public void runfasterSolTest() throws Exception {
		fs.run("sample2.json");
	}
}