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

package org.ibsplc.preconditiontests.util;
import static org.junit.Assert.assertNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

/**
 * Findbugs code analysis test-util for JUnit
 * @author Sandeep Rao
 *
 */
public class JUnitFindbugsTestWrapper {

	private PrintStream out;
	private ByteArrayOutputStream byteArrOutputStream;

	public int execute(String sourcePath) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, SecurityException, NoSuchMethodException, IOException {
		System.out.println("Starting Findbugs on folder "+sourcePath);
		
		PrintStream out2 = System.out;
		setupStreams();
		
		Class<?> cls = Class.forName("edu.umd.cs.findbugs.FindBugs2");
		Method main = cls.getMethod("main", String[].class);
		String[] params = {"-home","D:\\dev\\util\\findbugs-1.3.9","-excludeBugs","findBugsBaselineViolations.xml",sourcePath}; // init params 
		//String[] params = {"-help"}; 
		main.invoke(null, (Object) params); // static invoke
		
		byte[] byteArray = byteArrOutputStream.toByteArray();
		InputStream inputStream=new ByteArrayInputStream(byteArray);
		int len = inputStream.read(byteArray); 
		if(len>0){
			out2.print(new String(byteArray,Charset.forName("UTF-8")));
			assertNull(new String(byteArray,Charset.forName("UTF-8")));
		}
		 
		return len;
	}
	
	private void baselineViolations(String sourcePath)  throws IllegalArgumentException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, SecurityException, NoSuchMethodException, IOException {
			FileOutputStream fileOut = new FileOutputStream("findBugsBaselineViolations.xml");
			PrintStream out2 = new PrintStream(fileOut);
			setupStreams();			
			Class<?> cls = Class.forName("edu.umd.cs.findbugs.FindBugs2");
			Method main = cls.getMethod("main", String[].class);
			String[] params = {"-home","D:\\Dev\\util\\findbugs-1.3.9","-xml",sourcePath}; 
			main.invoke(null, (Object) params); // static invoke
			
			byte[] byteArray = byteArrOutputStream.toByteArray();
			InputStream inputStream=new ByteArrayInputStream(byteArray);
			int len = inputStream.read(byteArray); 
			out2.print(new String(byteArray,Charset.forName("UTF-8")));
			out2.close();
			fileOut.close();
	}

	private void setupStreams() {
		byteArrOutputStream = new ByteArrayOutputStream();
		out = new PrintStream(new BufferedOutputStream(byteArrOutputStream) );		
		System.setOut(out); 
	}
	
	public static void main(String[] args) throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException, IOException {
		//new FindbugsWrapper().execute("D:\\Dev\\Workspaces\\Java_WS_Cassowary\\findbugsWrapper\\target\\classes\\MalitiaActus.class");
		//new FindbugsWrapper().execute("D:/Eclipse_WorkSpaces/Build_workpspace/iLogisticsNeo/target/classes");
		String sourcePath = "D:/Eclipse_WorkSpaces/Build_workpspace/iLogisticsNeo/target/classes";
		if(args[0]!=null){
			sourcePath = args[0];
		}else{
			System.out.println("Usage: JUnitFindbugsTestWrapper <Path to classes>");
		}
		new JUnitFindbugsTestWrapper().baselineViolations(sourcePath);
	}

} 
