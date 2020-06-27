package com.dwiveddi.restapi;

import com.dwiveddi.restapi.config.RunnerInput;
import com.dwiveddi.testscommon.utils.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.util.*;

import static com.dwiveddi.restapi.RestApiExecutor.SUITE_VARIABLE_FILE;
import static com.dwiveddi.restapi.RestApiExecutor.TEST_VARIABLE_FILE;

/**
 * Created by dwiveddi on 4/6/2018.
 */
public class RestApiRunner {

    public static class Result{
        private boolean isPassed = true;
        private String resultFile = "";

        @Override
        public String toString() {
            return "Result{" +
                    "isPassed=" + isPassed +
                    ", resultFile='" + resultFile + '\'' +
                    '}';
        }

        public Result(boolean isPassed, String resultFile) {
            this.isPassed = isPassed;
            this.resultFile = resultFile;
        }
    }

    public static Result run(RunnerInput runnerInput){
        System.setProperty("testFile", runnerInput.getTestFile());
        if(runnerInput.getTestVariableFile() != null) {
            System.setProperty(TEST_VARIABLE_FILE, runnerInput.getTestVariableFile());
        }
        if(runnerInput.getSuiteVariableFile() != null) {
            System.setProperty(SUITE_VARIABLE_FILE, runnerInput.getSuiteVariableFile());
        }
        if(null != runnerInput.getSheetsToInclude() && null != runnerInput.getSheetsToIgnore()) {
            throw new IllegalArgumentException("[Ambiguous Situation] Both the variable sheetsToInclude and sheetsToIgnore are set. Please set only on of them");
        }
        if(null != runnerInput.getSheetsToIgnore()){
            try {
                System.setProperty("sheetsToIgnore", new ObjectMapper().writeValueAsString(runnerInput.getSheetsToIgnore()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Exception while serializing sheetsToIgnore", e);
            }
        }

        if(null != runnerInput.getSheetsToInclude()){
            try {
                System.setProperty("sheetsToInclude", new ObjectMapper().writeValueAsString(runnerInput.getSheetsToInclude()));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Exception while serializing sheetsToInclude", e);
            }
        }
        boolean isFilterToInclude = false;
        Set<String> filteredTags = new HashSet<>();
        if(null != runnerInput.getIncludeTags() && !runnerInput.getIncludeTags().isEmpty()){
            isFilterToInclude = true;
            filteredTags = runnerInput.getIncludeTags();
        }else if(null != runnerInput.getExcludeTags() && !runnerInput.getExcludeTags().isEmpty()){
            filteredTags = runnerInput.getExcludeTags();
        }

        try {
            System.setProperty("filteredTags", new ObjectMapper().writeValueAsString(filteredTags));
            System.setProperty("isFilterToInclude", String.valueOf(isFilterToInclude));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Exception while serializing filteredTags", e);
        }

        XmlSuite suite = new XmlSuite();
        XmlTest test = new XmlTest(suite);
        test.setClasses(Arrays.asList(new XmlClass("com.dwiveddi.restapi.RestApiExecutor")));
        TestNG testNG = new TestNG();
        if(null != runnerInput.getOutputDir()) {
            testNG.setOutputDirectory(runnerInput.getOutputDir());
        }
        testNG.setXmlSuites(Arrays.asList(suite));
        if(!runnerInput.isZephyrUpdateDisabled()) {
            testNG.setListenerClasses(Arrays.asList(RestApiListener.class));
        }
        Map<String, String> suiteParams = new HashMap<>();
        suiteParams.put("zephyrEnv",  runnerInput.getZephyrEnv());
        suiteParams.put("versionName",  runnerInput.getVersionName());
        suiteParams.put("cycleName",  runnerInput.getCycleName());
        suite.setParameters(suiteParams);
        testNG.run();
        System.out.println(testNG.hasFailure());
        System.out.println(testNG.hasSkip());
        System.out.println(testNG.getStatus());
        System.out.println("Detailed Report can be found at: "+testNG.getOutputDirectory()+"/index.html");
        return new Result(0 == testNG.getStatus() && !RestApiExecutor.isDataProviderFailed, "file:///"+testNG.getOutputDirectory()+"/index.html");
    }

    public static void main(String[] args) throws Exception {
        //run(new RunnerInput("conf/Book1.xlsx"));
        //run(new RunnerInput("conf/Book1.xlsx").confFile("conf/book1Variables.json"));
        //run(new RunnerInput("conf/Book1.xlsx").outputDir("C:/REPORTS"));
        //run(new RunnerInput("conf/Book1.xlsx").sheetsToIgnore("QueryParam", "NestedArray", "PayloadPropagation", "Initial"));
        System.out.println(run(new RunnerInput("C:/Users/dwiveddi/Desktop/ActionDriverAPI/Rules/Divya.xlsx")
                        //.testVariableFile("conf/book1Variables.json")
                        //.suiteVariableFile("conf/book1SuiteVariables.json")
                        .outputDir("C:/REPORTS")
                        .sheetsToInclude("Sheet2")
                        .zephyrEnv("conf/zephyrEnv.properties")
                        .versionName("Unscheduled")
                        .cycleName("abcd")
                        .excludeTags("A")
                        .includeTags("A")
                        //.setZephyrUpdateDisabled(true)
                //.sheetsToIgnore("ConfigFileInput", "QueryParam", "NestedArray", "PayloadPropagation", "Initial", "RandomString","InputFiles","SuiteVariable")
                ));
       /* System.out.println(((Map<String, Object>) GlobalVariables.INSTANCE.get("headers")).get("ContentType"));
        System.out.println(((Map<String, Object>) GlobalVariables.INSTANCE.get("headers")));
        System.out.println(((Map<String, Object>) GlobalVariables.INSTANCE.get("abc")));*/
       /* System.out.println(GlobalVariables.eval("${abc.hello}"));
        System.out.println(GlobalVariables.evalJson("${abc}"));
        System.out.println(GlobalVariables.eval("${headers.arrayY[0]}"));
        System.out.println(GlobalVariables.eval("${headers.ContentType}"));
        List<Integer> list = (List<Integer>)GlobalVariables.evalObject("${abc.z}");
        for(Integer integer : list){
            System.out.println(integer);
        }*/

        //System.out.println(GlobalVariables.eval("Vendorly-${randomString(10)}@gmail.com"));
        //run(new RunnerInput("conf/Book1.xlsx").confFile("conf/variables.json").outputDir("C:/REPORTS").sheetsToIgnore(new String[]{"QueryParam", "NestedArray", "OutputPropagation"}));
    }

}
