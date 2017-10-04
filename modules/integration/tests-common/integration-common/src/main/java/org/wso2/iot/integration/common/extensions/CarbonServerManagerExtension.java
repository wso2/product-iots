/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.iot.integration.common.extensions;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.context.beans.User;
import org.wso2.carbon.automation.engine.exceptions.AutomationFrameworkException;
import org.wso2.carbon.automation.engine.frameworkutils.CodeCoverageUtils;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.automation.engine.frameworkutils.ReportGenerator;
import org.wso2.carbon.automation.engine.frameworkutils.TestFrameworkUtils;
import org.wso2.carbon.automation.extensions.servers.utils.ArchiveExtractor;
import org.wso2.carbon.automation.extensions.servers.utils.ClientConnectionUtil;
import org.wso2.carbon.automation.extensions.servers.utils.FileManipulator;
import org.wso2.carbon.automation.extensions.servers.utils.ServerLogReader;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class CarbonServerManagerExtension {
    private static final Log log = LogFactory.getLog(CarbonServerManagerExtension.class);
    private Process process;
    private String carbonHome;
    private AutomationContext automationContext;
    private ServerLogReader inputStreamHandler;
    private ServerLogReader errorStreamHandler;
    private boolean isCoverageEnable = false;
    private String coverageDumpFilePath;
    private int portOffset = 0;
    private static final String SERVER_SHUTDOWN_MESSAGE = "Halting JVM";
    private static final String SERVER_STARTUP_MESSAGE = "Mgt Console URL";
    private static final long DEFAULT_START_STOP_WAIT_MS = 400000L;
    private static final String CMD_ARG = "cmdArg";
    private static int defaultHttpPort = Integer.parseInt("9763");
    private static int defaultHttpsPort = Integer.parseInt("9443");
    private static final long COVERAGE_DUMP_WAIT_TIME = 30000;

    public CarbonServerManagerExtension(AutomationContext context) {
        this.automationContext = context;
    }

    public synchronized void startServerUsingCarbonHome(String carbonHome, Map<String, String> commandMap) throws AutomationFrameworkException {
        if(this.process == null) {
            this.portOffset = this.checkPortAvailability(commandMap);
            Process tempProcess = null;

            try {
                if(!commandMap.isEmpty() && this.getPortOffsetFromCommandMap(commandMap) == 0) {
                    System.setProperty("carbon.home", carbonHome);
                }

                File commandDir = new File(carbonHome);
                log.info("Starting carbon server............. ");
                String scriptName = TestFrameworkUtils.getStartupScriptFileName(carbonHome);
                String[] parameters = this.expandServerStartupCommandList(commandMap);
                String[] cmdArray;
                if(System.getProperty("os.name").toLowerCase().contains("windows")) {
                    commandDir = new File(carbonHome + File.separator + "bin");
                    cmdArray = new String[]{"cmd.exe", "/c", scriptName + ".bat"};
                    cmdArray = this.mergePropertiesToCommandArray(parameters, cmdArray);
                    tempProcess = Runtime.getRuntime().exec(cmdArray, (String[])null, commandDir);
                } else {
                    cmdArray = new String[]{"sh", "bin/" + scriptName + ".sh"};
                    cmdArray = this.mergePropertiesToCommandArray(parameters, cmdArray);
                    tempProcess = Runtime.getRuntime().exec(cmdArray, (String[])null, commandDir);
                }

                this.errorStreamHandler = new ServerLogReader("errorStream", tempProcess.getErrorStream());
                this.inputStreamHandler = new ServerLogReader("inputStream", tempProcess.getInputStream());
                this.inputStreamHandler.start();
                this.errorStreamHandler.start();
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        try {
                            CarbonServerManagerExtension.this.serverShutdown(CarbonServerManagerExtension.this.portOffset);
                        } catch (Exception var2) {
                            CarbonServerManagerExtension.log.error("Error while server shutdown ..", var2);
                        }

                    }
                });
                ClientConnectionUtil.waitForPort(defaultHttpPort + this.portOffset, DEFAULT_START_STOP_WAIT_MS, false, (String)this
                        .automationContext.getInstance().getHosts().get("default"));
                long time = System.currentTimeMillis() + 60000L;

//                while(true) {
//                    if(this.inputStreamHandler.getOutput().contains("Mgt Console URL") || System.currentTimeMillis() >= time) {
//                        int httpsPort = defaultHttpsPort + this.portOffset;
//                        String backendURL = this.automationContext.getContextUrls().getSecureServiceUrl().replaceAll("(:\\d+)", ":" + httpsPort);
//                        User superUser = this.automationContext.getSuperTenant().getTenantAdmin();
//                        ClientConnectionUtil.waitForLogin(backendURL, superUser);
//                        log.info("Server started successfully.");
//                        break;
//                    }
//                }
                int httpsPort = defaultHttpsPort + this.portOffset;
                String backendURL = this.automationContext.getContextUrls().getSecureServiceUrl().replaceAll("(:\\d+)", ":" + httpsPort);
                User superUser = this.automationContext.getSuperTenant().getTenantAdmin();
                ClientConnectionUtil.waitForLogin(backendURL, superUser);
            } catch (XPathExpressionException | IOException var13) {
                throw new IllegalStateException("Unable to start server", var13);
            }

            this.process = tempProcess;
        }
    }

    private int checkPortAvailability(Map<String, String> commandMap) throws AutomationFrameworkException {
        int portOffset = this.getPortOffsetFromCommandMap(commandMap);
        if(ClientConnectionUtil.isPortOpen(defaultHttpPort + portOffset)) {
            throw new AutomationFrameworkException("Unable to start carbon server on port " + (defaultHttpPort + portOffset) + " : Port already in use");
        } else if(ClientConnectionUtil.isPortOpen(defaultHttpsPort + portOffset)) {
            throw new AutomationFrameworkException("Unable to start carbon server on port " + (defaultHttpsPort + portOffset) + " : Port already in use");
        } else {
            return portOffset;
        }
    }

    private String[] mergePropertiesToCommandArray(String[] parameters, String[] cmdArray) {
        if(parameters != null) {
            cmdArray = this.mergerArrays(cmdArray, parameters);
        }

        return cmdArray;
    }

    /**
     * Unzip carbon zip file and return the carbon home. Based on the coverage configuration in automation.xml
     * This method will inject jacoco agent to the carbon server startup scripts.
     *
     * @param carbonServerZipFile - Carbon zip file, which should be specified in test module pom
     * @return - carbonHome - carbon home
     * @throws IOException - If pack extraction fails
     */
    public synchronized String setUpCarbonHome(String carbonServerZipFile) throws IOException, AutomationFrameworkException {
        if(this.process != null) {
            return this.carbonHome;
        } else {
            int indexOfZip = carbonServerZipFile.lastIndexOf(".zip");
            if(indexOfZip == -1) {
                throw new IllegalArgumentException(carbonServerZipFile + " is not a zip file");
            } else {
                String fileSeparator = File.separator.equals("\\")?"\\":"/";
                if(fileSeparator.equals("\\")) {
                    carbonServerZipFile = carbonServerZipFile.replace("/", "\\");
                }

                String extractedCarbonDir = carbonServerZipFile.substring(carbonServerZipFile.lastIndexOf(fileSeparator) + 1, indexOfZip);
                FileManipulator.deleteDir(extractedCarbonDir);
                String extractDir = "carbontmp" + System.currentTimeMillis();
                String baseDir = System.getProperty("basedir", ".") + File.separator + "target";
                log.info("Extracting carbon zip file.. ");
                (new ArchiveExtractor()).extractFile(carbonServerZipFile, baseDir + File.separator + extractDir);
                this.carbonHome = (new File(baseDir)).getAbsolutePath() + File.separator + extractDir + File.separator + extractedCarbonDir;

                try {
                    this.isCoverageEnable = Boolean.parseBoolean(this.automationContext.getConfigurationValue("//coverage"));
                } catch (XPathExpressionException var8) {
                    throw new AutomationFrameworkException("Coverage configuration not found in automation.xml", var8);
                }

                if(this.isCoverageEnable) {
                    this.instrumentForCoverage();
                }

                return this.carbonHome;
            }
        }
    }

    public synchronized void serverShutdown(int portOffset) throws AutomationFrameworkException {
        if(this.process != null) {
            log.info("Shutting down server..");
            if(ClientConnectionUtil.isPortOpen(Integer.parseInt("9443") + portOffset)) {
                int httpsPort = defaultHttpsPort + portOffset;
                String url = null;

                try {
                    url = this.automationContext.getContextUrls().getBackEndUrl();
                } catch (XPathExpressionException var10) {
                    throw new AutomationFrameworkException("Get context failed", var10);
                }

                String backendURL = url.replaceAll("(:\\d+)", ":" + httpsPort);

                try {
                    ClientConnectionUtil.sendForcefulShutDownRequest(backendURL, this.automationContext.getSuperTenant().getContextUser().getUserName(), this.automationContext.getSuperTenant().getContextUser().getPassword());
                } catch (AutomationFrameworkException var8) {
                    throw new AutomationFrameworkException("Get context failed", var8);
                } catch (XPathExpressionException var9) {
                    throw new AutomationFrameworkException("Get context failed", var9);
                }

                long time = System.currentTimeMillis() + 300000L;

//                while(!this.inputStreamHandler.getOutput().contains("Halting JVM") && System.currentTimeMillis() < time) {
//                    ;
//                }

                log.info("Server stopped successfully...");
            }

            this.inputStreamHandler.stop();
            this.errorStreamHandler.stop();
            this.process.destroy();
            this.process = null;
            if(this.isCoverageEnable) {
                try {
                    log.info("Generating Jacoco code coverage...");
                    this.generateCoverageReport(new File(this.carbonHome + File.separator + "wso2"
                            + File.separator + "components" + File.separator + "plugins" + File.separator));
                } catch (IOException var7) {
                    log.error("Failed to generate code coverage ", var7);
                    throw new AutomationFrameworkException("Failed to generate code coverage ", var7);
                }
            }

            if(portOffset == 0) {
                System.clearProperty("carbon.home");
            }
        }

    }

    private void generateCoverageReport(File classesDir) throws IOException, AutomationFrameworkException {
        checkJacocoDataFileSizes(FrameworkPathUtil.getJacocoCoverageHome());
        CodeCoverageUtils.executeMerge(FrameworkPathUtil.getJacocoCoverageHome(), FrameworkPathUtil.getCoverageMergeFilePath());
        ReportGenerator reportGenerator = new ReportGenerator(new File(FrameworkPathUtil.getCoverageMergeFilePath()), classesDir, new File(CodeCoverageUtils.getJacocoReportDirectory()), (File)null);
        reportGenerator.create();
        log.info("Jacoco coverage dump file path : " + FrameworkPathUtil.getCoverageDumpFilePath());
        log.info("Jacoco class file path : " + classesDir);
        log.info("Jacoco coverage HTML report path : " + CodeCoverageUtils.getJacocoReportDirectory() + File.separator + "index.html");
    }

    public synchronized void restartGracefully() throws AutomationFrameworkException {
        try {
            int httpsPort = defaultHttpsPort + this.portOffset;
            String backendURL = this.automationContext.getContextUrls().getSecureServiceUrl().replaceAll("(:\\d+)", ":" + httpsPort);
            User superUser = this.automationContext.getSuperTenant().getTenantAdmin();
            ClientConnectionUtil.sendGraceFullRestartRequest(backendURL, superUser.getUserName(), superUser.getPassword());
        } catch (XPathExpressionException var5) {
            throw new AutomationFrameworkException("restart failed", var5);
        }

        long time = System.currentTimeMillis() + 300000L;

//        while(!this.inputStreamHandler.getOutput().contains("Halting JVM") && System.currentTimeMillis() < time) {
//            ;
//        }

        time = System.currentTimeMillis();

        while(System.currentTimeMillis() < time + 5000L) {
            ;
        }

        try {
            ClientConnectionUtil.waitForPort(Integer.parseInt((String)this.automationContext.getInstance().getPorts().get("https")), (String)this.automationContext.getInstance().getHosts().get("default"));
            ClientConnectionUtil.waitForLogin(this.automationContext);
        } catch (XPathExpressionException var4) {
            throw new AutomationFrameworkException("Connection attempt to carbon server failed", var4);
        }
    }

    private String[] expandServerStartupCommandList(Map<String, String> commandMap) {
        if(commandMap != null && commandMap.size() != 0) {
            String[] cmdParaArray = null;
            String cmdArg = null;
            if(commandMap.containsKey("cmdArg")) {
                cmdArg = (String)commandMap.get("cmdArg");
                cmdParaArray = cmdArg.trim().split("\\s+");
                commandMap.remove("cmdArg");
            }

            String[] parameterArray = new String[commandMap.size()];
            int arrayIndex = 0;
            Set<Map.Entry<String, String>> entries = commandMap.entrySet();

            String parameter;
            for(Iterator i$ = entries.iterator(); i$.hasNext(); parameterArray[arrayIndex++] = parameter) {
                Map.Entry<String, String> entry = (Map.Entry)i$.next();
                String key = (String)entry.getKey();
                String value = (String)entry.getValue();
                if(value != null && !value.isEmpty()) {
                    parameter = key + "=" + value;
                } else {
                    parameter = key;
                }
            }

            if(cmdArg != null) {
                commandMap.put("cmdArg", cmdArg);
            }

            if(cmdParaArray != null && cmdParaArray.length != 0) {
                return (String[]) ArrayUtils.addAll(parameterArray, cmdParaArray);
            } else {
                return parameterArray;
            }
        } else {
            return null;
        }
    }

    private int getPortOffsetFromCommandMap(Map<String, String> commandMap) {
        return commandMap.containsKey("-DportOffset")?Integer.parseInt((String)commandMap.get("-DportOffset")):0;
    }

    private String[] mergerArrays(String[] array1, String[] array2) {
        return (String[])ArrayUtils.addAll(array1, array2);
    }

    /**
     * This methods will insert jacoco agent settings into startup script under JAVA_OPTS
     *
     * @param scriptName - Name of the startup script
     * @throws IOException - throws if shell script edit fails
     */
    private void insertJacocoAgentToShellScript(String scriptName) throws IOException {
        String jacocoAgentFile = CodeCoverageUtils.getJacocoAgentJarLocation();
        this.coverageDumpFilePath = FrameworkPathUtil.getCoverageDumpFilePath();
        CodeCoverageUtils.insertStringToFile(new File(this.carbonHome + File.separator + "bin" + File.separator + scriptName + ".sh"), new File(this.carbonHome + File.separator + "tmp" + File.separator + scriptName + ".sh"), "-Dwso2.server.standalone=true", "-javaagent:" + jacocoAgentFile + "=destfile=" + this.coverageDumpFilePath + "" + ",append=true,includes=" + CodeCoverageUtils.getInclusionJarsPattern(":") + " \\");
    }

    /**
     * This methods will insert jacoco agent settings into windows bat script
     *
     * @param scriptName - Name of the startup script
     * @throws IOException - throws if shell script edit fails
     */
    private void insertJacocoAgentToBatScript(String scriptName) throws IOException {
        String jacocoAgentFile = CodeCoverageUtils.getJacocoAgentJarLocation();
        this.coverageDumpFilePath = FrameworkPathUtil.getCoverageDumpFilePath();
        CodeCoverageUtils.insertJacocoAgentToStartupBat(new File(this.carbonHome + File.separator + "bin" + File.separator + scriptName + ".bat"), new File(this.carbonHome + File.separator + "tmp" + File.separator + scriptName + ".bat"), "-Dcatalina.base", "-javaagent:" + jacocoAgentFile + "=destfile=" + this.coverageDumpFilePath + "" + ",append=true,includes=" + CodeCoverageUtils.getInclusionJarsPattern(":"));
    }

    /**
     * This method will check the OS and edit server startup script to inject jacoco agent
     *
     * @throws IOException - If agent insertion fails.
     */
    private void instrumentForCoverage() throws IOException, AutomationFrameworkException {
        String scriptName = TestFrameworkUtils.getStartupScriptFileName(this.carbonHome);
        if(System.getProperty("os.name").toLowerCase().contains("windows")) {
            this.insertJacocoAgentToBatScript(scriptName);
            if(log.isDebugEnabled()) {
                log.debug("Included files " + CodeCoverageUtils.getInclusionJarsPattern(":"));
                log.debug("Excluded files " + CodeCoverageUtils.getExclusionJarsPattern(":"));
            }
        } else {
            this.insertJacocoAgentToShellScript(scriptName);
        }

    }

    /**
     * To check jacoco file sizes and wait for them to get created..
     *
     * @param filePath File Path of the jacoco data files.
     */
    private void checkJacocoDataFileSizes(String filePath) {
        Collection<File> fileSetsCollection = FileUtils
                .listFiles(new File(filePath), new RegexFileFilter("[^s]+(." + "(?i)(exec))$"),
                        DirectoryFileFilter.DIRECTORY);

        for (File inputFile : fileSetsCollection) {
            if (inputFile.isDirectory()) {
                continue;
            }
            //retry to check whether exec data file is non empty.
            waitForCoverageDumpFileCreation(inputFile);
        }
    }

    /**
     * This is to wait for jacoco exe file creation.
     *
     * @param file File that need to be created.
     */
    private void waitForCoverageDumpFileCreation(File file) {
        long currentTime = System.currentTimeMillis();
        long waitTime = currentTime + COVERAGE_DUMP_WAIT_TIME;

        while (waitTime > System.currentTimeMillis()) {
            if (file.length() > 0) {
                log.info("Execution data file non empty file size in KB : " + file.length() / 1024);
                break;
            } else {

                try {
                    log.warn("Execution data file is empty file size in KB : " + file.length() / 1024);
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                    log.warn("Sleep interrupted ", ignored);
                }

            }
        }
    }

}
