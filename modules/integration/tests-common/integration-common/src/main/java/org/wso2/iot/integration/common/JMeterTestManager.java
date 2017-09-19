/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.iot.integration.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.util.ShutdownClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.extensions.jmeter.JMeterInstallationProvider;
import org.wso2.carbon.automation.extensions.jmeter.JMeterResult;
import org.wso2.carbon.automation.extensions.jmeter.JMeterTest;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class JMeterTestManager {

    private static final Pattern PAT_ERROR = Pattern.compile(".*\\s+ERROR\\s+.*");
    private static final Log log = LogFactory.getLog(JMeterTestManager.class);

    private String jmeterLogLevel = "INFO";

    private File testFile = null;
    private File jmeterHome = null;
    private File jmeterLogFile = null;

    private DateFormat fmt = new SimpleDateFormat("yyMMdd-HH-mm-ss");

    private File jmeterProps = null;

    public void runTest(JMeterTest jMeterTest)
            throws AutomationFrameworkException {
        JMeterResult results;

        // Init JMeter
        jmeterHome = JMeterInstallationProvider.getInstance().getJMeterHome();

        testFile = jMeterTest.getTestFile();
        //setting jmeter.properties file parameter
        try {
            setJMeterPropertyFile(jMeterTest);
        } catch (IOException e) {
            throw new AutomationFrameworkException("Set property failed " + e.getMessage(), e);
        }

        if (jMeterTest.getLogLevel() != null) {
            jmeterLogLevel = jMeterTest.getLogLevel();
        }

        results = executeMe();
        //        checkForErrors();
        log.info("for more info. " + results.getFileName());
        if (results.getErrorCount() > 0) {
            throw new AutomationFrameworkException("Test Failed. " + results.getErrorCount() + " Error/s Found.\n"
                    + results.getErrorList().toString() + "\nRefer "
                    + results.getFileName() + " for test result");
        }

        if (results.getFailureCount() > 0) {
            throw new AssertionError("Test Failed. " + results.getFailureCount() + " Assertion Failure/s.\n"
                    + results.getAssertList().toString() + "\nRefer "
                    + results.getFileName() + " for test result");
        }

    }

    private JMeterResult executeMe() throws AutomationFrameworkException {

        try {
            addLogFile(testFile.getName());
        } catch (IOException e) {
            throw new AutomationFrameworkException("Can't add log file " + e.getMessage(), e);
        }
        Boolean resultState = true;
        JMeterResult results;

        String resultFile = executeTest(testFile);
        try {
            // Force shutdown
            StandardJMeterEngine.stopEngineNow();
            ShutdownClient.main(new String[]{"Shutdown"});

        } catch (IOException ex) {
            log.error(ex);
            resultState = false;
        }
        results = resultValidator(resultFile);
        results.setFileName(resultFile);
        results.setExecutionState(resultState);

        return results;
    }

    private void checkForErrors() throws AutomationFrameworkException, IOException {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(jmeterLogFile), Charset.defaultCharset()));

            String line;
            while ((line = in.readLine()) != null) {
                if (PAT_ERROR.matcher(line).find()) {
                    throw new AutomationFrameworkException("There were test errors, see logfile '" + jmeterLogFile + "' for further information");
                }
            }
            in.close();
        } catch (IOException e) {
            throw new IOException("Can't read log file", e);
        }
    }

    private void setJMeterPropertyFile(JMeterTest jMeterTest) throws IOException {
        if (jMeterTest.getJMeterPropertyFile() == null) {
            log.info("Loading default jmeter.properties...");
            jmeterProps = JMeterInstallationProvider.getInstance().getJMeterPropertyFile();
            System.setProperty("jmeter_properties",
                    File.separator + "bin" + File.separator + "jmeter.properties");
        } else {
            log.info("Loading custom jmeter.properties from " + jMeterTest.getJMeterPropertyFile().getCanonicalPath());
            jmeterProps = jMeterTest.getJMeterPropertyFile();
            System.setProperty("jmeter_properties", jmeterProps.getCanonicalPath());

        }
    }

    private String executeTest(File test) throws AutomationFrameworkException {
        String reportFileName;
        String reportFileFullPath;
        JMeter jmeterInstance = new JMeter();
        try {
            log.info("Executing test: " + test.getCanonicalPath());
            reportFileName = test.getName().substring(0,
                    test.getName().lastIndexOf(".")) + "-"
                    + fmt.format(new Date()) + ".jmeterResult" + ".jtl";

            File reportDir = JMeterInstallationProvider.getInstance().getReportDir();
            reportFileFullPath = reportDir.toString() + File.separator + reportFileName;
            List<String> argsTmp = Arrays.asList("-n",
                    "-t", test.getCanonicalPath(),
                    "-l", reportDir.toString() + File.separator + reportFileName,
                    "-p", jmeterProps.toString(),
                    "-d", jmeterHome.getCanonicalPath(),
                    "-L", "jorphan=" + jmeterLogLevel,
                    "-L", "jmeter.util=" + jmeterLogLevel);

            List<String> args = new ArrayList<String>();

            args.addAll(argsTmp);

            SecurityManager oldManager = System.getSecurityManager();

            UncaughtExceptionHandler oldHandler = Thread.getDefaultUncaughtExceptionHandler();
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                public void uncaughtException(Thread t, Throwable e) {
                    if (e instanceof ExitException && ((ExitException) e).getCode() == 0) {
                        return;    //Ignore
                    }
                    log.error("Error in thread " + t.getName());
                }
            });

            try {
                logParamsAndProps(args);

                jmeterInstance.start(args.toArray(new String[]{}));

                BufferedReader in = new BufferedReader(new InputStreamReader(
                        new FileInputStream(jmeterLogFile), Charset.defaultCharset()));

                while (!checkForEndOfTest(in)) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }

            } catch (ExitException e) {
                if (e.getCode() != 0) {
                    throw new AutomationFrameworkException("Test failed " + e.getMessage(), e);
                }
            } catch (Exception e) {
                log.error(e);

            } finally {
                System.setSecurityManager(oldManager);
                Thread.setDefaultUncaughtExceptionHandler(oldHandler);
            }
        } catch (IOException e) {
            throw new AutomationFrameworkException("Can't execute test " + e.getMessage(), e);
        }
        return reportFileFullPath;
    }

    private void logParamsAndProps(List<String> args) {
        log.debug("Starting JMeter with the following parameters:");
        for (String arg : args) {
            log.debug(arg);
        }
        Properties props = System.getProperties();
        Set<Object> keysUnsorted = props.keySet();
        SortedSet<Object> keys = new TreeSet<Object>(keysUnsorted);
        log.debug("... and the following properties:");
        for (Object k : keys) {
            String key = (String) k;
            String value = props.getProperty(key);
            log.debug(key + " = " + value);
        }
    }

    private JMeterResult resultValidator(String fileName) throws AutomationFrameworkException {

        JMeterResult result = null;
        XMLStreamReader parser = null;
        FileInputStream inputStream = null;
        File file = new File(fileName);
        List<String> assertionFailureList;
        List<String> errorList;

        if (file.exists()) {
            try {
                inputStream = new FileInputStream(file);
                parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);

                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document doc = dBuilder.parse(file);
                doc.getDocumentElement().normalize();


                errorList = new ArrayList<String>();
                result = new JMeterResult();
                assertionFailureList = new ArrayList<String>();

                NodeList nodeList = doc.getElementsByTagName("httpSample");

                for (int temp = 0; temp < nodeList.getLength(); temp++) {

                    String responseMessage;
                    String label;
                    String name = null;
                    String error = null;
                    String failure = null;
                    String failureMessage = null;

                    Node node = nodeList.item(temp);

                    responseMessage = node.getAttributes().getNamedItem("s").getTextContent();
                    label = node.getAttributes().getNamedItem("lb").getTextContent();

                    if ("false".equalsIgnoreCase(responseMessage)) {
                        result.increaseErrorCount();
                        String errorMessage = label + " > " + responseMessage;
                        if (!errorList.contains(errorMessage)) {
                            errorList.add(errorMessage);
                        }
                    }

                    if (node.getNodeType() == Node.ELEMENT_NODE) {

                        Element element = (Element) node;

                        if (element.getElementsByTagName("name").getLength() != 0) {
                            name = element.getElementsByTagName("name").item(0).getTextContent();
                        }
                        if (element.getElementsByTagName("error").getLength() != 0) {
                            error = element.getElementsByTagName("error").item(0).getTextContent();
                        }
                        if (element.getElementsByTagName("failure").getLength() != 0) {
                            failure = element.getElementsByTagName("failure").item(0).getTextContent();
                        }
                        if (element.getElementsByTagName("failureMessage").getLength() != 0) {
                            failureMessage = element.getElementsByTagName("failureMessage").item(0).getTextContent();
                        }

                        if ("true".equalsIgnoreCase(failure) || "true".equalsIgnoreCase(error)) {
                            result.increaseFailureCount();
                            String assertionFailure = label + " : " + name + " > " + failureMessage;
                            if (!assertionFailureList.contains(assertionFailure)) {
                                assertionFailureList.add(assertionFailure);
                            }
                        }
                    }
                }

                result.setErrorList(errorList);
                result.setAssertList(assertionFailureList);
            } catch (FileNotFoundException e) {
                throw new AutomationFrameworkException("Result File is not Created");
            } catch (ParserConfigurationException e) {
                throw new AutomationFrameworkException("Result File is not Created");
            } catch (SAXException e) {
                throw new AutomationFrameworkException("Result File is not Created");
            } catch (IOException e) {
                throw new AutomationFrameworkException("Result File is not Created");
            } catch (XMLStreamException e) {
                throw new AutomationFrameworkException("Result File is not Created");
            } finally {

                if (parser != null) {
                    try {
                        parser.close();
                    } catch (XMLStreamException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        ///ignore
                    }
                }
            }
        } else {
            throw new AutomationFrameworkException("Result File is not Created");
        }
        return result;
    }


    private boolean checkForEndOfTest(BufferedReader in) throws IOException {
        boolean testEnded = false;
        try {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.contains("Test has ended")) {
                    testEnded = true;
                    break;
                }
            }

        } catch (IOException e) {
            throw new IOException("Can't read log file", e);
        }
        return testEnded;
    }


    private static class ExitException extends SecurityException {

        private static final long serialVersionUID = 5544099211927987521L;
        public int _rc;

        public ExitException(int rc) {
            super(Integer.toString(rc));
            _rc = rc;
        }

        public int getCode() {
            return _rc;
        }
    }

    private void addLogFile(String fileName) throws IOException {

        jmeterLogFile = new File(JMeterInstallationProvider.getInstance().getLogDir().getCanonicalPath()
                + File.separator + fileName.substring(0, fileName.lastIndexOf(".")) + "-" + fmt.format(new Date()) + ".log");
        if (!jmeterLogFile.createNewFile()) {
            log.error("unable to create log file");
        }
        try {
            System.setProperty("log_file", jmeterLogFile.getCanonicalPath());
        } catch (IOException e) {
            throw new IOException("Can't get canonical path for log file " + e.getMessage(), e);
        }

    }
}