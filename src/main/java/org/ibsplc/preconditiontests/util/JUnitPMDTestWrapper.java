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
import java.io.PrintStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sourceforge.pmd.PMD;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  PMD test-util for JUnit
 * 
 * @author Sandeep Rao
 */

public abstract class JUnitPMDTestWrapper {

	private static PrintStream out;
	private static PrintStream err;		
	private static ByteArrayOutputStream baosOut;
	private static ByteArrayOutputStream baosErr;		
	private static PrintStream psOut;
	private static PrintStream psErr;
	private static Map<String, List<String>> baslineViolations;
	private static Map<String, List<String>> actualViolations;

	/**
	 * Runs the PMD JUnit test wrapper on a specific folder based on a given
	 * rule set file in the same folder
	 * 
	 * @param testClassInstance
	 *            The JUnit test instance
	 * @param folderToCheck
	 *            The folder that should be tested
	 * @param ruleFileName
	 *            Name of the PMD rule set file that should be located in the
	 *            same folder
	 * @throws Exception 
	 */

	public static void run(final Object testClassInstance,final String folderToCheck, final String ruleFileName) throws Exception {	
		File fileFolderToCheck = new File(folderToCheck);
		
		URL ruleFileURL = testClassInstance.getClass().getResource(ruleFileName);
		String outputType = "xml";
		String rules = URLDecoder.decode(ruleFileURL.toString(), "UTF-8");
		String[] arguments = new String[] { "-d",fileFolderToCheck.getAbsolutePath(), "-f",outputType, "-R",rules };
		/*String[] arguments = new String[] { fileFolderToCheck.getAbsolutePath(), outputType, rules };*/	//Earlier PMD version syntax

		startupMessage(fileFolderToCheck);
		validateFileFolders(testClassInstance, ruleFileName, fileFolderToCheck);
		setupStreamRedirects();

		PMD.main(arguments);
		
		postReportCleanup();
		writeXMLReport();		 
		readViolations();						
		
		boolean hasViolations = checkForNewViolations(baslineViolations,actualViolations);
		assertFalse(hasViolations);  //Comparison of Baseline and actual violation reports to or final result
		/*if(hasViolations){
			throw new Exception("SCA Preconditions failed");
		}*/
	}
	
	private static void readViolations() throws ParserConfigurationException,
	SAXException, IOException {
		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		PMDParsingHandler defaultHandler = new PMDParsingHandler();
		saxParser.parse("pmd_baselineViolations.xml", defaultHandler);
		baslineViolations = defaultHandler.getViolationsMap();
		defaultHandler.clear();
		saxParser.parse("pmd_Violations.xml", defaultHandler);
		actualViolations = defaultHandler.getViolationsMap();
		defaultHandler.clear();
	}

	private static final class PMDParsingHandler extends DefaultHandler {
		
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
			if (qName.equalsIgnoreCase("violation")) {

				vIdentifier = attributes.getValue(0) + ":"+ attributes.getValue(1) + ":" + attributes.getValue(2)
					  + ":" + attributes.getValue(3) + ":"+ attributes.getValue(4)+":"+ attributes.getValue(8);
				//Sample demonstrating order :
				//beginline="62" endline="62" begincolumn="25" endcolumn="59" rule="UnusedPrivateMethod" ruleset="Unused Code" package="com.ibsplc.ilogisticsneo.core.company.bo.impl" class="CompanyBOImpl"
				//method="isListEmpty" externalInfoUrl="http://pmd.sourceforge.net/pmd-5.0.2/rules/java/unusedcode.html#UnusedPrivateMethod" priority="3"
				violationsList.add(vIdentifier);

			}
		}

	}

	private static void writeXMLReport() throws FileNotFoundException, IOException {
		String linesOut[] = baosOut.toString().split("\\r?\\n");
		List<String> rowsOut = new ArrayList<String>();

		for (String line : linesOut) {
			if (!line.isEmpty()
					&& line.indexOf("suppressed by Annotation") == -1
					&& line.indexOf("No problems found!") == -1
					&& line.indexOf("Error while processing") == -1
					&& line.indexOf("Use Rule name") == -1) {
				rowsOut.add(line);
			}
		}

		File violationsReport = new File("pmd_Violations.xml");
		FileOutputStream fileOutputStream = new FileOutputStream(
				violationsReport);
		//System.out.println("Found " + rowsOut.size() + " errors");
		for (String error : rowsOut) {
			//System.out.println(error);
			fileOutputStream.write((error + "\r\n").getBytes());
		}

		fileOutputStream.close();
	}

	private static void postReportCleanup() throws IOException {
		System.setOut(out);
		System.setErr(err);
		
		psOut.close();
		psErr.close();
		baosOut.close();
		baosErr.close();
	}

	private static void setupStreamRedirects() {
		out= System.out;
		err= System.err;

		baosOut = new ByteArrayOutputStream();
		baosErr = new ByteArrayOutputStream();
		
		psOut = new PrintStream(baosOut);
		psErr = new PrintStream(baosErr);
		
		System.setOut(psOut);
		System.setErr(psErr);
	}

	private static void startupMessage(File fileFolderToCheck) {
		System.out.println("Starting PMD code analyzer test on folder '"
				+ fileFolderToCheck.getAbsolutePath() + "'.");
	}

	private static void validateFileFolders(final Object testClassInstance,
			final String ruleFileName, File fileFolderToCheck)
			throws FileNotFoundException {
		if (!fileFolderToCheck.exists()) {
			throw new FileNotFoundException("The folder to check '"
					+ fileFolderToCheck.getAbsolutePath() + "' does not exist.");
		}

		if (!fileFolderToCheck.isDirectory()) {
			throw new FileNotFoundException("The folder to check '"
					+ fileFolderToCheck.getAbsolutePath()
					+ "' is not a directory.");
		}

		if (testClassInstance.getClass().getResource(ruleFileName) == null) {
			throw new FileNotFoundException("The rule set file '"
					+ ruleFileName + "' does not exist in the same folder as '"
					+ testClassInstance.getClass().getSimpleName() + "'.");
		}
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
						System.out.println("new violation found at :"+fileName+" method:" +violationDetails[5]+", rule="+violationDetails[4]+", Lines: "+violationDetails[0]+" - "+violationDetails[1]+", [begincolumn="+violationDetails[2]+" endcolumn="+violationDetails[3]+"]"); 
						
						//Sample demonstrating order --> beginline="62" endline="62" begincolumn="25" endcolumn="59" rule="UnusedPrivateMethod" method="isListEmpty"
						
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

}