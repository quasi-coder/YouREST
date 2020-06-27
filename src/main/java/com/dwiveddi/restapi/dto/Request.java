package com.dwiveddi.restapi.dto;

import com.dwiveddi.mapper.csv.annotation.CsvMapped;
import com.dwiveddi.testscommon.templateengine.FreemarkerTemplateEngine;

import java.util.Map;

/**
 * Created by dwiveddi on 2/6/2018.
 */
public class Request {
    private static final FreemarkerTemplateEngine engine = FreemarkerTemplateEngine.getInstance();


    @CsvMapped.Column(index = 3) private String queryParams;
    @CsvMapped.Column(index = 4) private String headers;
    //@CsvMapped.Column(index = 4, converterMethod = "convertTopMap") private Map<String,String> headers;
    @CsvMapped.Column(index = 5) private String payload;

/*    public Map<String, String> convertTopMap(String headers) throws IOException {
        headers = engine.generate(headers, FreemarkerTemplateEngine.getInstance().getGlobalMap());
        if(!headers.isEmpty()) {
            return new ObjectMapper().readValue(headers, Map.class);
        }
        return null;
    }*/
    public Request(String queryParams, String payload, String headers) {
        this.queryParams = queryParams;
        this.payload = payload;
        this.headers = headers;
    }

    public void format(Map<String, Object> map) {
        this.queryParams = engine.generate(this.queryParams, map);
        this.payload = engine.generate(this.payload, map);
        this.headers = engine.generate(this.headers, map);
    }

    public Request() {
    }


    @Override
    public String toString() {
        return "com.dwiveddi.restapi.dto.Request{" +
                "queryParams='" + queryParams + '\'' +
                ", payload='" + payload + '\'' +
                ", headers=" + headers +
                '}';
    }

    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }


}
