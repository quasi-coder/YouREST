package com.dwiveddi.restapi;

import com.dwiveddi.restapi.utils.HttpClientUtils;
import com.dwiveddi.testscommon.utils.JsonUtils;
import com.dwiveddi.testscommon.utils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.testng.Assert;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.dwiveddi.restapi.utils.HttpClientUtils.getHttpClient;

/**
 * Created by dwiveddi on 6/11/2018.
 */
public class ZephyrHelper {
    private static final Logger LOGGER = Logger.getLogger(ZephyrHelper.class.getName());
    private static String USERNAME ;
    private static String PASSWORD ;
    private static String BASE_URL ;
    private static String ZAPI_URL ;
    private static String PROJECT_KEY;

    private static String projectId, versionId, cycleId;

    public enum Status{
        UNEXECUTED(-1), PASS(1), FAIL(2), WIP(3), BLOCKED(4);

        private int value;

        Status(int value){
            this.value = value;
        }
        public int getValue(){
            return value;
        }

    }

    public static void init(String zephyrConfigFile,String versionName, String cycleName){
        try {
            PropertyUtils.load(zephyrConfigFile);
            USERNAME =  PropertyUtils.getValue("USERNAME");
            PASSWORD =  PropertyUtils.getValue("PASSWORD");
            BASE_URL =  String.format("http://%s/jira", PropertyUtils.getValue("JIRA_HOST"));
            ZAPI_URL =  BASE_URL + "/rest/zapi/latest/";
            PROJECT_KEY =  PropertyUtils.getValue("PROJECT_KEY");
            projectId = getProjectId();
            System.out.println("============================ ProjectId = " + projectId);
            versionId = getVersionId(versionName);
            System.out.println("============================ VersionId = "+versionId);
            cycleId = createTestCycle(cycleName);
            System.out.println("============================ CycleId = "+ cycleId);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private static HttpRequestBase getHTTPBase(String path, String httpMethod,String queryParams,Map<String,String> headers, String payload) throws UnsupportedEncodingException {
        HttpRequestBase httpRequestBase = HttpClientUtils.getHTTPBase(path, httpMethod, queryParams, headers, payload);
        HttpClientUtils.setBasicAuthorizationHeader(httpRequestBase, USERNAME, PASSWORD);
        return httpRequestBase;
    }

    private static String getProjectId() throws IOException {
        String url =BASE_URL+"/rest/api/2/project/"+PROJECT_KEY;
        HttpResponse response = HttpClientUtils.getHttpClient().execute(getHTTPBase(url, "GET", null, null, null));
        int expectedStatusCode = 200;
        int actualStatusCode = response.getStatusLine().getStatusCode();
        try{
            Assert.assertEquals(actualStatusCode, expectedStatusCode, String.format("Status Code Should Match: actualStatusCode = '%s',expectedStatusCode = '%s'", actualStatusCode, expectedStatusCode));
        }catch(AssertionError error){
            LOGGER.log(Level.SEVERE, "Assertion Error occurs", error);
        }
        String actualPayload = IOUtils.toString(response.getEntity().getContent());
        Map<String, String> actualMap = (Map<String, String>) JsonUtils.fromJson(actualPayload, Map.class);
        String projectId = actualMap.get("id");
        return projectId;
    }

    private static String getVersionId(String versionName) throws IOException {
        String url =BASE_URL+"/rest/api/2/project/" + PROJECT_KEY + "/versions";
        HttpRequestBase httpRequestBase = getHTTPBase(url.trim(), "GET", null, null, null);
        HttpResponse response = HttpClientUtils.getHttpClient().execute(httpRequestBase);
        String actualPayload = IOUtils.toString(response.getEntity().getContent());
        Map<String, String>[] mapArr = (Map<String, String>[])JsonUtils.fromJson(actualPayload, Map[].class);
        if(!versionName.equals("Unscheduled")) {
            for (Map<String, String> jsonElement : mapArr) {
                if (jsonElement.get("name").equals(versionName.trim())) {
                    return  jsonElement.get("id");
                }
            }
        }else{
            return  "-1";
        }
        throw new IllegalArgumentException(String.format("No versionName found with name = '%s'. Please ask the admin to add the versionName. ",versionName));
    }

    private static String findTestCycle(String cycleName) throws IOException {
        String queryParams = "projectId=" + projectId + "&" + "versionId=" + versionId;
        String url = ZAPI_URL + "cycle";
        HttpRequestBase httpRequestBase = getHTTPBase(url.trim(), "GET", queryParams, null, null);
        HttpResponse response = HttpClientUtils.getHttpClient().execute(httpRequestBase);
        int expectedStatusCode = 200;
        int actualStatusCode = response.getStatusLine().getStatusCode();
        try{
            Assert.assertEquals(actualStatusCode, expectedStatusCode, String.format("Status Code Should Match: actualStatusCode = '%s',expectedStatusCode = '%s'", actualStatusCode, expectedStatusCode));
        }catch(AssertionError error){
            LOGGER.log(Level.SEVERE, "Assertion Error occurs", error);
        }
        String actualPayload = IOUtils.toString(response.getEntity().getContent());
        Map<String, Object> actualMap = (Map<String, Object>) JsonUtils.fromJson(actualPayload, Map.class);
        for (Map.Entry<String, Object> entry : actualMap.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map<String, Object> cycleInfo = (Map<String, Object>) entry.getValue();
                if (cycleInfo.get("name").equals(cycleName.trim())) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    private static String createTestCycle(String cycleName) throws IOException {
        String cycleId = findTestCycle(cycleName);
        if(cycleId == null) {
            String payload = "{\"name\":\"" + cycleName + "\"," +
                    "\"projectId\":\"" + projectId + "\"," +
                    "\"versionId\":\"" + versionId + "\"}";
            String url = ZAPI_URL + "cycle";
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            HttpRequestBase httpRequestBase = getHTTPBase(url.trim(), "POST", null, headers, payload);
            HttpResponse response = HttpClientUtils.getHttpClient().execute(httpRequestBase);
            int expectedStatusCode = 200;
            int actualStatusCode = response.getStatusLine().getStatusCode();
            try{
                Assert.assertEquals(actualStatusCode, expectedStatusCode, String.format("Status Code Should Match: actualStatusCode = '%s',expectedStatusCode = '%s'", actualStatusCode, expectedStatusCode));
            }catch(AssertionError error){
                LOGGER.log(Level.SEVERE, "Assertion Error occurs", error);
            }
            String actualPayload = IOUtils.toString(response.getEntity().getContent());
            Map<String, String> actualMap = (Map<String, String>) JsonUtils.fromJson(actualPayload, Map.class);
            cycleId = actualMap.get("id");
        }
        return  cycleId;
    }

    private static String createExecution(String issueKey) throws IOException {
        String executionId = "";

        String payload ="{\"issueId\":\""+getIssueId(issueKey)+"\"," +
                "\"versionId\":\""+versionId+"\"," +
                "\"cycleId\":\""+cycleId+"\"," +
                "\"projectId\":\""+projectId+"\"}";
        String url = ZAPI_URL + "execution";
        Map<String,String> headers = new HashMap<>();
        headers.put("Content-Type","application/json");
        HttpRequestBase httpRequestBase = getHTTPBase(url.trim(), "POST", null, headers, payload);
        HttpResponse response = getHttpClient().execute(httpRequestBase);
        int expectedStatusCode = 200;
        int actualStatusCode = response.getStatusLine().getStatusCode();
        try{
            Assert.assertEquals(actualStatusCode, expectedStatusCode, String.format("Status Code Should Match: actualStatusCode = '%s',expectedStatusCode = '%s'", actualStatusCode, expectedStatusCode));
        }catch(AssertionError error){
            LOGGER.log(Level.SEVERE, "Assertion Error occurs", error);
        }
        String actualPayload = IOUtils.toString(response.getEntity().getContent());
        Map<String, Object> actualMap = (Map<String, Object>) JsonUtils.fromJson(actualPayload, Map.class);
        for (Map.Entry<String, Object> entry : actualMap.entrySet()){
            executionId = entry.getKey();
        }
        return executionId;
    }

    private static String getIssueId(String issueKey) throws IOException {
        String url =BASE_URL+"/rest/api/2/issue/"+issueKey.trim();
        HttpRequestBase httpRequestBase = getHTTPBase(url.trim(), "GET", null, null, null);
        HttpResponse response = getHttpClient().execute(httpRequestBase);
        int expectedStatusCode = 200;
        int actualStatusCode = response.getStatusLine().getStatusCode();
        try {
            Assert.assertEquals(actualStatusCode, expectedStatusCode, String.format("Status Code Should Match: actualStatusCode = '%s',expectedStatusCode = '%s'", actualStatusCode, expectedStatusCode));
        }catch(AssertionError error){
            LOGGER.log(Level.SEVERE, "Assertion Error occurs", error);
        }
        String actualPayload = IOUtils.toString(response.getEntity().getContent());
        Map<String, String> actualMap = (Map<String, String>) JsonUtils.fromJson(actualPayload, Map.class);
        String issueId = actualMap.get("id");
        return issueId;
    }

    public static void updateTestCaseStatus(String issueKey, Status status){
        try {
            Integer executionStatus = Integer.valueOf(status.getValue());
            String executionId = createExecution(issueKey);
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            String url = ZAPI_URL + "execution/" + executionId + "/execute";
            String payload = "{\"status\":" + executionStatus + "}";
            HttpRequestBase httpRequestBase = getHTTPBase(url.trim(), "PUT", null, headers, payload);
            HttpResponse response = getHttpClient().execute(httpRequestBase);
            int expectedStatusCode = 200;
            int actualStatusCode = response.getStatusLine().getStatusCode();
            Assert.assertEquals(actualStatusCode, expectedStatusCode, String.format("Status Code Should Match: actualStatusCode = '%s',expectedStatusCode = '%s'", actualStatusCode, expectedStatusCode));
        }catch (Exception e){
            LOGGER.log(Level.SEVERE, "Assertion Error occurs", e);
            throw new RuntimeException(e);
        }
    }

}
