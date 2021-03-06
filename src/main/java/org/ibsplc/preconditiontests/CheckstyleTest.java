/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 */

package org.ibsplc.preconditiontests;

import org.ibsplc.preconditiontests.util.JUnitCheckstyleTestWrapper;
import org.junit.Test;


/**
 * Contains the Checkstyle tests
 * 
 * @author Christoffer Pettersson, christoffer@christoffer.me
 */

public class CheckstyleTest {

	/**
	 * Tests the SRC-folder
	 * 
	 * @throws Exception If any errors occur
	 */
 
	@Test
	public void testSrc() throws Exception {

		JUnitCheckstyleTestWrapper.run(this, "src/main/java/", "/checkstyle.xml");

	}

	/**
	 * Tests the TEST-folder
	 * 
	 * @throws Exception If any errors occur
	 */

	/*@Test
	public void testTest() throws Exception {

		JUnitCheckstyleTestWrapper.run(this, "src/test/java/", "/checkstyle.xml");

	}*/

}