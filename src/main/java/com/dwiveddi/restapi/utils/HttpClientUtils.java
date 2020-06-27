package com.dwiveddi.restapi.utils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.testng.Reporter;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by dwiveddi on 2/6/2018.
 */
public class HttpClientUtils {

     public static HttpClient getHttpClient() {
        RequestConfig globalConfig =  RequestConfig.custom()
                        .setConnectTimeout(1000*10) //10 sec, connection establishment timeout
                        .setConnectionRequestTimeout(1000 * 10) //10sec , connect request time, ping response from server
                        .setSocketTimeout(6000 * 10) //60 secs, wait for the response to come from server
                        .setCookieSpec(CookieSpecs.DEFAULT)
                        .setRedirectsEnabled(false)
                        .build();

        HttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(globalConfig)
                .setMaxConnTotal(500)
                .setMaxConnPerRoute(100)
                .setConnectionTimeToLive(600, TimeUnit.SECONDS)
                .evictIdleConnections(600, TimeUnit.SECONDS)
                .build();
        return httpClient;
    }

    public static HttpRequestBase getHTTPBase(String path, String httpMethod,String queryParams,Map<String,String> headers, String payload) throws UnsupportedEncodingException {
        HttpRequestBase httpRequestBase = null;
        if(null!= queryParams && !queryParams.isEmpty()){
            if(queryParams.startsWith("?")){
                path = path + queryParams;
            }else {
                path = path + "?" + queryParams;
            }
        }
        switch (httpMethod){
            case "GET" :  httpRequestBase =  new HttpGet(path); break;
            case "POST": httpRequestBase = new HttpPost(path); break;
            case "PUT": httpRequestBase =  new HttpPut(path); break;
            case "PATCH": httpRequestBase = new HttpPatch(path); break;
            case "DELETE" : httpRequestBase =  new HttpDelete(path); break;
            default: throw new IllegalArgumentException("Unhandled httpMethod " + httpMethod);
        }
        if(httpRequestBase instanceof HttpEntityEnclosingRequestBase){
            HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase = (HttpEntityEnclosingRequestBase) httpRequestBase;
            if(null != payload) {
                if(isMultipart(payload)){
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    String lines[] = payload.split("\\n");
                    boolean isLineContinuation = false;
                    StringBuilder prevText = new StringBuilder();
                    for(String line :  lines){
                        String arr[] = line.substring(2).split("=");
                        String key = arr[0].trim();
                        String value = arr[1].trim();
                        if(value.startsWith("@")){
                            builder.addBinaryBody(key, new File(value.substring(1)));//after '@' which at index 0
                        }else{
                            builder.addTextBody(key, value);
                        }
                    }
                    httpEntityEnclosingRequestBase.setEntity(builder.build());
                }else {
                    httpEntityEnclosingRequestBase.setEntity(new StringEntity(payload));
                }
            }
        }
        if(null != headers) {
            httpRequestBase.setHeaders(convertHeaderMapToList(headers));
        }

        Reporter.log("####### Request-Method = " + httpMethod);
        Reporter.log("####### Request-URL = " + path);
        Reporter.log("####### Request-Headers = " + headers);
        Reporter.log("####### Request-Body" + payload);


        return httpRequestBase;
    }

    public static void setBasicAuthorizationHeader(HttpRequestBase httpRequestBase, String username, String password){
        if (!username.isEmpty() && !password.isEmpty()) {
            Base64.Encoder encoder = Base64.getEncoder();
            String encodedString = encoder.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
            Header authHeader = new BasicHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedString);
            httpRequestBase.setHeader(authHeader);
        }
    }

    public static boolean isMultipart(String payload){
        return payload.startsWith("F ");
    }
    public static Map<String, String> convertHeadersListToMap(Header[] headers){
        Map<String, String> map = new HashMap<>();
        for (Header header : headers) {
            map.put(header.getName(), header.getValue());
        }
        return map;
    }

    public static Header[] convertHeaderMapToList(Map<String, String> map){
        Header[] headers = new Header[map.size()];
        int i = 0;
        for (Map.Entry<String, String> entry :  map.entrySet()) {
            headers[i++] = new BasicHeader(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    public static void main(String[] args) {
        String[] arr = "a=a1\nb=1\nc=1".split("\\n");
        for(String line : arr){
            System.out.println(Arrays.asList(line.split("=")));
        }
        System.out.println(Arrays.asList(arr));

    }
}
