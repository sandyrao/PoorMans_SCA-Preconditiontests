
package org.ibsplc.preconditiontests.util;
 
import static org.junit.Assert.assertTrue;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
 
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
 
import com.vladium.emma.Command;
import com.vladium.emma.IAppConstants;
import com.vladium.emma.report.ReportProcessor;
/*
import MailServiceTest;
import RegionServiceTest;
import UserServiceTest;
*/
 
@RunWith(Suite.class)
@Suite.SuiteClasses({
           /* UserServiceTest.class ,
              RegionServiceTest.class ,
              MailServiceTest.class 
			*/
})
 
public class CoveragePreconditionTestSuite {  
private static Map statsMap=new HashMap();
      
       @BeforeClass
       public static void emmaInstrument() {
              String[] args={"-verbose","-m", "overwrite","-cp", "target/classes"};
              Command command = Command.create ("instr", IAppConstants.APP_NAME_LC + " instr", args);
              command.run();
       }
      
       @AfterClass
       public static void analyzeEmmaCoverageData() throws FileNotFoundException,
                     ParserConfigurationException, SAXException, IOException {
              File file = new File("coverage.ec");
              Properties settings=new Properties();
              com.vladium.emma.rt.RT.dumpCoverageData( file,  true,  true);
              final ReportProcessor processor = ReportProcessor.create ();
            processor.setDataPath (new String[]{"coverage.em","coverage.ec"});
            processor.setSourcePath (null);
            processor.setReportTypes (new String[]{"xml"});
              processor.setPropertyOverrides (settings);
            processor.run ();
                    
              File coverageFile = new File("coverage.xml");
              if (!coverageFile.exists()) {
                     throw new FileNotFoundException("The coverage file '"
                                  + coverageFile.getAbsolutePath() + "' does not exist.");
              }
              SAXParserFactory factory = SAXParserFactory.newInstance();
              SAXParser saxParser = factory.newSAXParser();
              DefaultHandler handler = new DefaultHandler() {
                     private boolean isAggrStats;
                     public void startElement(String uri, String localName,
                                  String qName, Attributes attributes) throws SAXException {
                           // coverage type="class, %" value="100% (185/185)"                         
                           if(qName.equalsIgnoreCase("all")){
                                  isAggrStats=true;
                           }
                           if (qName.equalsIgnoreCase("coverage")) {
                                  if(isAggrStats){
                                         Pattern pattern=Pattern.compile("[\\d]*%");
                                         String value = attributes.getValue("value");
                                         String type = attributes.getValue("type");
                                         Matcher matcher = pattern.matcher(value);
                                        matcher.find();
                              String group = matcher.group();
                                         statsMap.put(type, Integer.parseInt( group.substring(0, group.indexOf("%")) ) );    
                                         System.out.println(type.substring(0, type.length()-3)+" "+value);
                                  }
                           }                         
                           if(qName.equalsIgnoreCase("package")){
                                  isAggrStats=false;
                           }
                     }
 
                     public void endElement(String uri, String localName, String qName)
                                  throws SAXException {}
 
                     public void characters(char ch[], int start, int length)
                                  throws SAXException {}
 
              };
              testCoverageThresholds(coverageFile, saxParser, handler);
       }
      
       private static void testCoverageThresholds(File coverageFile, SAXParser saxParser,
                     DefaultHandler handler) throws SAXException, IOException {
              saxParser.parse(coverageFile, handler);
             
              int classThreshold =100;
              int methodThreshold=50;
              int blockThreshold =60; 
              int lineThreshold  =60;  
              assertTrue("classThreshold not met" ,( (Integer)statsMap.get("class, %")) >=classThreshold);
              assertTrue("methodThreshold not met",( (Integer)statsMap.get("method, %"))>=methodThreshold);
              assertTrue("blockThreshold not met" ,( (Integer)statsMap.get("block, %")) >=blockThreshold);
              assertTrue("lineThreshold not met"  ,( (Integer)statsMap.get("line, %"))  >=lineThreshold);
       }
}
