package com.dwiveddi.restapi;

import com.dwiveddi.mapper.excel.ExcelMapper;
import com.dwiveddi.restapi.dto.PayloadStructure;
import com.dwiveddi.restapi.dto.RequestResponseCombination;
import com.dwiveddi.restapi.utils.XmlValidator;
import com.dwiveddi.testscommon.templateengine.FreemarkerTemplateEngine;
import com.dwiveddi.testscommon.utils.FileUtils;
import com.dwiveddi.testscommon.utils.JsonUtils;
import com.dwiveddi.testscommon.variables.GlobalVariables;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.dwiveddi.restapi.utils.HttpClientUtils.*;

/**
 * Created by dwiveddi on 4/6/2018.
 */
public class RestApiExecutor {
    public static final String TEST_VARIABLE_FILE =  "testVariableFile";
    public static final String SUITE_VARIABLE_FILE = "suiteVariableFile";
    private static HttpClient httpClient = getHttpClient();
    String keyPrefix = "";
    public static boolean isDataProviderFailed = false;

    @DataProvider(name = "combinations")
    public Iterator<Object[]> dataFromConfigFiles()  {
        try {
            String confFile = System.getProperty(SUITE_VARIABLE_FILE);
            if(!(null == confFile || confFile.isEmpty() || confFile.startsWith("$"))) {
                String generated = FreemarkerTemplateEngine.getInstance().generate(FileUtils.readFileAsString(confFile), new HashMap<>());
                GlobalVariables.INSTANCE.putAll((Map)JsonUtils.fromJson(generated, Map.class));
            }
            List<RequestResponseCombination> combinations = new ArrayList<>();
            String confDir = System.getProperty("testFile");
            List<String> listOfFilePaths = new ArrayList<>();
            populateFileNames(confDir, listOfFilePaths, new String[]{".xlsx", ".json"});
            ExcelMapper<RequestResponseCombination> excelMapper = new ExcelMapper<RequestResponseCombination>(RequestResponseCombination.class);
            for (String filePath : listOfFilePaths) {
                if (filePath.endsWith(".json")) {
                    //combinations.addAll(data(filePath));
                } else if (filePath.endsWith(".xlsx")) {
                    String sheetsToIgnore = System.getProperty("sheetsToIgnore");
                    String sheetsToInclude = System.getProperty("sheetsToInclude");

                    Set<String> ignoredSheets = null, includeSheets = null;
                    if(null == sheetsToIgnore || sheetsToIgnore.isEmpty()){
                        ignoredSheets = new HashSet<>();
                    }else{
                        ignoredSheets = (Set)JsonUtils.fromJson(sheetsToIgnore, Set.class);
                    }
                    if(null == sheetsToInclude || sheetsToInclude.isEmpty()){
                        includeSheets = new HashSet<>();
                    }else{
                        includeSheets = (Set)JsonUtils.fromJson(sheetsToInclude, Set.class);
                    }
                    List<RequestResponseCombination> list;
                    if(includeSheets.isEmpty()) {
                       list = excelMapper.getListByIgnoringSheetNames(filePath, ignoredSheets, 15, 2);
                    }else{
                        list = excelMapper.getListBySheetNames(filePath, includeSheets, 15, 2);
                    }
                    for(RequestResponseCombination combination : list){
                        combination.setSource(filePath);
                    }
                    combinations.addAll(list);
                }
            }
            List<Object[]> list = new ArrayList<>();
            Set<String> filteredTags = (Set)JsonUtils.fromJson(System.getProperty("filteredTags"), Set.class);//Input1
            boolean isFilterToInclude = Boolean.parseBoolean(System.getProperty("isFilterToInclude")); //Input2
            for (RequestResponseCombination combination : combinations) {
                boolean toAdd = isFilterToInclude ? false : true; //toAdd = false
                for(String filterTags : filteredTags){
                    if(combination.getTags().contains(filterTags)){
                        toAdd  = isFilterToInclude ? true: false; break; //toAdd = true
                    }
                }
                if(toAdd) {
                    list.add(new Object[]{combination});
                }
            }
            return list.iterator();
        }catch (Exception e){
            e.printStackTrace();
            isDataProviderFailed = true;
            throw new RuntimeException("Error while ... + "+e.getMessage() + " - " + e.getCause(), e);
        }
    }

    private void populateFileNames(String filePath, List<String> fileNames, String[] allowedFileExtensions){
        File file = new File(filePath);
        if(!file.exists())
            throw new IllegalArgumentException("File Not Found. filePath = "+filePath);
        if(file.isDirectory()){
            for(File innerFile : file.listFiles()){
                populateFileNames(innerFile.getAbsolutePath(), fileNames, allowedFileExtensions);
            }
        }else{
            for(String allowedFileExtension : allowedFileExtensions){
                if(file.getAbsolutePath().endsWith(allowedFileExtension)){
                    fileNames.add(file.getAbsolutePath()); break;
                }
            }
        }
    }

    /*public List<RequestResponseCombination> data(String filePath) throws IOException {
        File file =  new File(filePath);
        Api[] apiArr = new ObjectMapper().readValue(file, Api[].class);
        List<RequestResponseCombination> listOfCombinations = new ArrayList<>();
        for(Api api : apiArr){
            for(RequestResponseCombination combination : api.getRequestResponseCombinations()){
                if(null == combination.getUrl()){
                    combination.setUrl(api.getDefaultUrl());
                }
                if(null == combination.getMethod()){
                    combination.setMethod(api.getDefaultMethod());
                }
                if(null == combination.getRequest()){
                    combination.setRequest(api.getDefaultRequest());
                }
                if(null == combination.getRequest().getHeaders()){
                    combination.getRequest().setHeaders(api.getDefaultRequest().getHeaders());
                }
                if(null == combination.getResponse()){
                    combination.setResponse(api.getDefaultResponse());
                }
                if(null == combination.getResponse().getHeaders()){
                    combination.getResponse().setHeaders(api.getDefaultResponse().getHeaders());
                }
                listOfCombinations.add(combination);
            }
        }
        return listOfCombinations;
    }*/

    @Test(dataProvider = "combinations")
    public void testRestApi(RequestResponseCombination combination) throws IOException {
        Reporter.log(String.format("####### sourceFile = '%s',\n TestCaseID = '%s'", combination.getSource(), combination.getId()));
        String confFile = System.getProperty(TEST_VARIABLE_FILE);
        if(!(null == confFile || confFile.isEmpty() || confFile.startsWith("$"))) {
            String generated = FreemarkerTemplateEngine.getInstance().generate(FileUtils.readFileAsString(confFile), new HashMap<>());
            GlobalVariables.INSTANCE.putAll((Map)JsonUtils.fromJson(generated, Map.class));
        }
        combination.format(GlobalVariables.INSTANCE);
        HttpRequestBase httpRequestBase = getHTTPBase(combination.getUrl().trim(), combination.getMethod(),combination.getRequest().getQueryParams(), convertToMap(combination.getRequest().getHeaders()), combination.getRequest().getPayload());
        HttpResponse response = getHttpClient().execute(httpRequestBase);
        Map<String, String> responseHeaders = convertHeadersListToMap(response.getAllHeaders());
        int expectedStatusCode= combination.getResponse().getStatusCode();
        String actualPayload = IOUtils.toString(response.getEntity().getContent());
        Reporter.log("####### Response-Payload = " + actualPayload);
        Reporter.log("####### Response-StatusCode = " + response.getStatusLine().getStatusCode());
        Reporter.log("####### Response-StatusCode-Desc = " + response.getStatusLine().getReasonPhrase());

        //Assertions
        if(0 != combination.getResponse().getStatusCode()){
            Assert.assertEquals(response.getStatusLine().getStatusCode(), expectedStatusCode, format("Status Code Should Match", combination));
        }
        if(null != combination.getResponse().getPayload() && !combination.getResponse().getPayload().isEmpty()) {
            String expected = jsonOneLine(combination.getResponse().getPayload());
            String actual = jsonOneLine(actualPayload);
            Assert.assertEquals(actual,expected,format("Payload Should match", combination));
        }
        for(Map.Entry<String, String>  expectedHeader : convertToMap(combination.getResponse().getHeaders()).entrySet()){
            //Assert.assertTrue("ActualResponseHeader should contain a key = "+expectedHeader.getKey(), responseHeaders.containsKey(expectedHeader.getKey())); //1. Checking presence oof key
            String expectedValue = expectedHeader.getValue();
            String actualValue = responseHeaders.get(expectedHeader.getKey());
            Assert.assertEquals( actualValue, expectedValue);  //2. Match values
        }
        if(PayloadStructure.XML.equals(combination.getResponse().getPayloadStructure())){
            XmlValidator.validateXMLBySchema(combination.getResponse().getXmlSchemaPath(), actualPayload);
        }else {
            List expectedJsonAttributes = combination.getResponse().getJsonAttributes();
            digestPayload(combination.getResponse().isPayloadJsonValdationRequired(), actualPayload, combination.getResponse().getPayloadStructure()
                    , expectedJsonAttributes, keyPrefix, combination.getVariableName());
        }


    }

    private Map<String, String> convertToMap(String headers) throws IOException {
        if (!headers.isEmpty()) {
            return (Map)JsonUtils.fromJson(headers, Map.class);
        }
        return new HashMap<>();
    }

    private String format(String msg, RequestResponseCombination combination){
        return "TestCaseId = "+combination.getId()+"; "+msg;
    }

    private void digestPayload(boolean isValidationRequired, String content, PayloadStructure payloadStructure, List expectedJsonAttributes, String keyPrefix, String variableName){
        Object o = null;
        switch(payloadStructure){
            case STRING:
                o = content; break;
            case ARRAY_OF_STRING   :
                try {
                    o = JsonUtils.fromJson(content, String[].class);
                }catch(Exception e){
                    Assert.assertTrue( false,"The content is not of type String[] Exception  = "+e.getMessage());
                }
                break;
            case ARRAY_OF_INTEGERS :
                try {
                    o = JsonUtils.fromJson(content, Integer[].class);
                }catch(Exception e){
                    Assert.assertTrue(false,"The content is not of type Integer[]. Exception = "+e.getMessage());
                }
                break;
            case ARRAY_OF_JSON     :
                try {
                    Map<String, Object>[] mapArr = (Map<String, Object>[])JsonUtils.fromJson(content, Map[].class);
                    if(isValidationRequired) {
                        for (Map actualMap : mapArr) {
                            validateJsonStructure(expectedJsonAttributes, actualMap, keyPrefix);
                        }
                    }
                    o = mapArr;
                }catch(IOException e){
                    Assert.assertTrue(false,"The content is not of type Map[]. Exception = "+e.getMessage());
                }
                break;
            case JSON              :
                try {
                    Map<String, Object> actualMap = (Map<String, Object>)JsonUtils.fromJson(content, Map.class);
                    if(isValidationRequired) {
                        validateJsonStructure(expectedJsonAttributes, actualMap, keyPrefix);
                    }
                    o = actualMap;
                }catch(IOException e){
                    Assert.assertTrue(false, "The content is not of type Map. Exception = "+e.getMessage());
                }
                break;
        }
        GlobalVariables.INSTANCE.put(variableName, o);
    }

    private void validateJsonStructure(List expectedJsonAttributes, Object objectToValidate,String keyPrefix) throws IOException {
        PayloadStructure payloadStructure = null;
        List list = new ArrayList<>();
        if(objectToValidate instanceof  Map){
            list.add(objectToValidate);
            payloadStructure = PayloadStructure.JSON;
        }else if(objectToValidate instanceof List){
            list = (List)objectToValidate;
            if(list.size() == 0){
                return ;
            }else{
                if(list.get(0) instanceof Map){
                    payloadStructure = PayloadStructure.ARRAY_OF_JSON;
                }else{
                    payloadStructure = PayloadStructure.ARRAY;
                }
            }
        }
        if(payloadStructure.equals(PayloadStructure.ARRAY_OF_JSON) || payloadStructure.equals(PayloadStructure.JSON)){
            for(Object o : list){
                Map actualResponseJsonAsMap = (Map) o;
                for (Object expectedJsonAttribute : expectedJsonAttributes) {
                    if (expectedJsonAttribute instanceof Map) {
                        Map<String, Object> expectedKeyValPair = (Map<String, Object>) expectedJsonAttribute;
                        for (Map.Entry<String, Object> expectedEntry : expectedKeyValPair.entrySet()) {
                            Assert.assertTrue(actualResponseJsonAsMap.containsKey(expectedEntry.getKey()),String.format("The actual response should contain a key with name =  %s%s ", keyPrefix, expectedEntry.getKey()));
                            if (expectedEntry.getValue() instanceof List) {
                                validateJsonStructure((List) expectedEntry.getValue(), actualResponseJsonAsMap.get(expectedEntry.getKey()),keyPrefix+expectedEntry.getKey()+"." );
                            } else {
                                Assert.assertEquals(actualResponseJsonAsMap.get(expectedEntry.getKey()),expectedEntry.getValue(),
                                        String.format("The value of key = '%s%s' should match with expectedValue",keyPrefix,expectedEntry.getKey())
                                );
                            }
                        }
                    } else if (expectedJsonAttribute instanceof String) {
                        Assert.assertTrue(actualResponseJsonAsMap.containsKey(expectedJsonAttribute),"The json response should contain key = " + expectedJsonAttribute);
                    } else {
                        throw new IllegalArgumentException(String.format("o of class = %s, is not handled", expectedJsonAttribute.getClass()));
                    }
                }
            }
        }else if(payloadStructure.equals(PayloadStructure.ARRAY)){
            for (Object expectedJsonAttribute : expectedJsonAttributes) {
                Assert.assertTrue(list.contains(expectedJsonAttribute), String.format("The value of key = '%s', which is a List/Array should contain expectedValue = '%s'", keyPrefix, expectedJsonAttribute));
            }
        }
    }

    private String jsonOneLine(String s) {
        return s.trim().replaceAll(" ","").replaceAll("\\n","");
    }
}
