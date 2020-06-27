package com.dwiveddi.restapi.dto;

import com.dwiveddi.mapper.csv.annotation.CsvMapped;
import com.dwiveddi.testscommon.templateengine.FreemarkerTemplateEngine;
import org.testng.Reporter;

import java.util.*;

/**
 * Created by dwiveddi on 2/6/2018.
 */
public class RequestResponseCombination {

    private static final FreemarkerTemplateEngine engine = FreemarkerTemplateEngine.getInstance();
    private String source;
    @CsvMapped.Column(index = 0) private String id;
    @CsvMapped.Column(index = 13) private String desc;
    @CsvMapped.Column(index = 1) private String url;
    @CsvMapped.Column(index = 2) private String method;
    @CsvMapped.NestedColumn(index = {3,4,5}) private Request request;
    @CsvMapped.NestedColumn(index = {6,7,8,9,10,11})private Response response;
    @CsvMapped.Column(index = 12) private String variableName;
    @CsvMapped.Column(index = 14, converterMethod = "split") private Set<String> tags;

    public Set<String> split(String str){
        Set<String> set = new HashSet<>();
        String arr[] = str.split(",");
        for(String s : arr){
            set.add(s.trim());
        }
        return set;
    }
    public void format(Map<String, Object> map){
        this.url = engine.generate(this.url, map);
        this.method = engine.generate(this.method, map);
        this.request.format(map);
        this.response.format(map);
    }

    @Override
    public String toString() {
        return "RequestResponseCombination{" +
                "source='" + source + '\'' +
                ", id='" + id + '\'' +
                ", desc='" + desc + '\'' +
                ", url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", request=" + request +
                ", response=" + response +
                ", variableName='" + variableName + '\'' +
                ", tags=" + tags +
                '}';
    }

    public Set<String> getTags() {
        return tags;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

}
