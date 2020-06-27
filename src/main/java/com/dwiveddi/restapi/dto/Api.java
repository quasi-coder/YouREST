package com.dwiveddi.restapi.dto;


import java.util.List;

/**
 * Created by dwiveddi on 2/6/2018.
 */
public class Api {

    private String defaultUrl;
    private String defaultMethod;
    private Request defaultRequest;
    private Response defaultResponse;
    private List<RequestResponseCombination> requestResponseCombinations;

    @Override
    public String toString() {
        return "com.dwiveddi.restapi.dto.Api{" +
                "defaultUrl='" + defaultUrl + '\'' +
                ", defaultMethod='" + defaultMethod + '\'' +
                ", defaultRequest=" + defaultRequest +
                ", defaultResponse=" + defaultResponse +
                ", requestResponseCombinations=" + requestResponseCombinations +
                '}';
    }

    public String getDefaultUrl() {
        return defaultUrl;
    }

    public void setDefaultUrl(String defaultUrl) {
        this.defaultUrl = defaultUrl;
    }

    public String getDefaultMethod() {
        return defaultMethod;
    }

    public void setDefaultMethod(String defaultMethod) {
        this.defaultMethod = defaultMethod;
    }

    public Request getDefaultRequest() {
        return defaultRequest;
    }

    public void setDefaultRequest(Request defaultRequest) {
        this.defaultRequest = defaultRequest;
    }

    public Response getDefaultResponse() {
        return defaultResponse;
    }

    public void setDefaultResponse(Response defaultResponse) {
        this.defaultResponse = defaultResponse;
    }

    public List<RequestResponseCombination> getRequestResponseCombinations() {
        return requestResponseCombinations;
    }

    public void setRequestResponseCombinations(List<RequestResponseCombination> requestResponseCombinations) {
        this.requestResponseCombinations = requestResponseCombinations;
    }
}
