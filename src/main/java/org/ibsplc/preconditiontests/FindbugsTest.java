package org.ibsplc.preconditiontests;

import org.ibsplc.preconditiontests.util.JUnitFindbugsTestWrapper;
import org.junit.Test;


public class FindbugsTest {
	/**
	 * Tests the SRC-folder
	 * 
	 * @throws Exception If any errors occur
	 */

	@Test
	public void testSrc() throws Exception {
		String sourcePath = "";
		String classPathArg = System.getProperty("sourcePath");
		if(classPathArg==null || "".equals(classPathArg) ){
			System.out.println("Please specify -Dsourcepath=<Path to classes>");
		}
		sourcePath = classPathArg;
		new JUnitFindbugsTestWrapper().execute(sourcePath);

	}

	/**
	 * Tests the TEST-folder
	 * 
	 * @throws Exception If any errors occur
	 */

/*	@Test
	public void testTest() throws Exception {

	
	}*/
}
