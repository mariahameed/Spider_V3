package Maria_Hameed.Spider_V3;

import static org.junit.Assert.fail;

import org.junit.Test;

import junit.framework.TestCase;

public class testcase1 extends TestCase {
	@Test
	public void test() {
		
		//this test case tests tests if the exception handling is done correctly
		//if an exception is thrown, it fails. else pass
	  try {
		  App s = new App();
		} catch (Exception ex) {
			fail( "Exception thrown" );
		}
	}

}
