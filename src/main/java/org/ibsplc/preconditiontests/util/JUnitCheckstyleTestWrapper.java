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

import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.XMLLogger;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * Checkstyle test-util for JUnit
 *
 * @author Sandeep Rao
 */

public abstract class JUnitCheckstyleTestWrapper {

	private static ByteArrayOutputStream sos;
	private static Map<String, List<String>> baslineViolations;
	private static Map<String, List<String>> actualViolations;

	/**
	 * Runs the JUnit Checkstyle test wrapper on a specific folder based on a given rule set file in the same folder
	 * 
	 * @param testClassInstance The JUnit test instance
	 * @param folderToCheck The folder that should be tested
	 * @param ruleFileName Name of the Checkstyle rule set file that should be located in the same folder
	 * @throws CheckstyleException  If any error occur
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */

	public static void run(final Object testClassInstance, final String folderToCheck, final String ruleFileName) throws CheckstyleException, IOException, ParserConfigurationException, SAXException {

		File fileFolderToCheck = new File(folderToCheck);

		System.out.println("Starting Checkstyle on folder '" + fileFolderToCheck.getAbsolutePath() + "'..");

		/*
		 * Validations
		 */

		if (!fileFolderToCheck.exists()) {
			throw new FileNotFoundException("The folder to check '" + fileFolderToCheck.getAbsolutePath() + "' does not exist.");
		}

		if (!fileFolderToCheck.isDirectory()) {
			throw new FileNotFoundException("The folder to check '" + fileFolderToCheck.getAbsolutePath() + "' is not a directory.");
		}

		if (testClassInstance.getClass().getResource(ruleFileName) == null) {
			throw new FileNotFoundException("The rule set file '" + ruleFileName + "' does not exist in the same folder as '" + testClassInstance.getClass().getSimpleName() + "'.");
		}

		/*
		 * Files
		 */

		List<File> files = new ArrayList<File>();
		listFiles(files, fileFolderToCheck, "java");
		System.out.println("Found " + files.size() + " Java source files.");

		if (files.isEmpty()) {
			Assert.fail("Found no Java source files. Configuration error?");
		}

		/*
		 * Listener
		 */

		sos = new ByteArrayOutputStream();
		AuditListener listener = new XMLLogger(sos, false);

		/*
		 * Configuration
		 */

		InputSource inputSource = new InputSource(testClassInstance.getClass().getResourceAsStream(ruleFileName));
		Configuration configuration = ConfigurationLoader.loadConfiguration(inputSource, new PropertiesExpander(System.getProperties()), false);

		/*
		 * Create checker
		 */

		Checker checker = new Checker();
		checker.setModuleClassLoader(Checker.class.getClassLoader());
		checker.configure(configuration);
		checker.addListener(listener);

		int errors = checker.process(files);	//Invoke Checkstyle violations scanning process
		
		//System.out.println("Found " + errors + " check style errors.");
		//System.out.println(sos.toString());
		
		checker.destroy();
		writeXMLReport();		
		readViolations();

		assertFalse(checkForNewViolations(baslineViolations,actualViolations));  //Comparison of Baseline and actual violation reports to or final result
		
		/*
		 * Clean up
		 */

	}
	
	private static final class checkstyleParsingHandler extends DefaultHandler {
		
		Map<String, List<String>> fileMap = new HashMap<String, List<String>>();
		List<String> violationsList;
		String vIdentifier = new String();

		public void clear() {
			fileMap = new HashMap<String, List<String>>();
		}

		public Map<String, List<String>> getViolationsMap() {
			return fileMap;
		}

		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			if (qName.equalsIgnoreCase("file")) {
				String value = attributes.getValue(0);
				violationsList = new ArrayList<String>();
				fileMap.put(value, violationsList);

			}
			if (qName.equalsIgnoreCase("error")) {
				vIdentifier = attributes.getValue( "line"  ) + ":"
							+ attributes.getValue("column" ) + ":"
							+ attributes.getValue("source" ) + ":"
							+ attributes.getValue("message");
				//line="162" column="17" severity="error" message="Missing a Javadoc comment." source="com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocMethodCheck"
				violationsList.add(vIdentifier);
			}
		}

	}
	
	private static void writeXMLReport() throws FileNotFoundException, IOException {
		String linesOut[] = sos.toString().split("\\r?\\n");
		List<String> rowsOut = new ArrayList<String>();

		for (String line : linesOut) {
				rowsOut.add(line);
		}

		File violationsReport = new File("checkstyle_Violations.xml");
		FileOutputStream fileOutputStream = new FileOutputStream(violationsReport);
		//System.out.println("Found " + rowsOut.size() + " errors");
		for (String error : rowsOut) {
			//System.out.println(error);
			fileOutputStream.write((error + "\r\n").getBytes());
		}

		fileOutputStream.close();
	}
	
	private static void readViolations() throws ParserConfigurationException,
		SAXException, IOException {
		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		checkstyleParsingHandler defaultHandler = new checkstyleParsingHandler();
		saxParser.parse("checkstyle_baselineViolations.xml", defaultHandler);
		baslineViolations = defaultHandler.getViolationsMap();
		defaultHandler.clear();
		saxParser.parse("checkstyle_Violations.xml", defaultHandler);
		actualViolations = defaultHandler.getViolationsMap();
		defaultHandler.clear();
	}
	
	private static boolean checkForNewViolations(Map<String, List<String>> violationsMap1, Map<String, List<String>> violationsMap2) {
		boolean flag=false;
		String[] violationDetails ;
		for (String fileName : violationsMap2.keySet()) {
			 List<String> violationList1 = violationsMap1.get(fileName);	//old (baseline) list
			 List<String> violationList2 = violationsMap2.get(fileName);	//new list
			if(violationList1!=null){
				for (String violationKey : violationList2) {
					if(! violationList1.contains(violationKey)){	//false to say old list doesn't have the violation, hence new violation detected.
						violationDetails= violationKey.split(":");
						System.out.println("new violation found at :"+fileName+",  message="+violationDetails[3]+", Line: "+violationDetails[0]+", [column="+violationDetails[1]+"]");
						
						//Sample demonstrating order --> 
						//line="162" column="17" source="com.puppycrawl.tools.checkstyle.checks.javadoc.JavadocMethodCheck" message="Missing a Javadoc comment." 
						
						flag= true;
					}
				}
			 }else{
				 System.out.println("new file with violations found :" +fileName);
				 flag= true;	// new violation present in new list
			 }
		}
		return flag;  //list 2,ie. new list, doesn't contain violation .. which is good because the violation disappeared in new result.
	}

	/**
	 * Lists all files in a given folder
	 */

	private static void listFiles(final List<File> files, final File folder, final String extension) {
		if (folder.canRead()) {
			if (folder.isDirectory()) {
				for (File f : folder.listFiles()) {
					listFiles(files, f, extension);
				}
			} else if (folder.toString().endsWith("." + extension)) {
				files.add(folder);
			}
		}
	}

}