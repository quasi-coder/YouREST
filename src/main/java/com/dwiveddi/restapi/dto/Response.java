package com.dwiveddi.restapi.dto;


import com.dwiveddi.mapper.csv.annotation.CsvMapped;
import com.dwiveddi.testscommon.templateengine.FreemarkerTemplateEngine;
import com.dwiveddi.testscommon.utils.JsonUtils;
import com.dwiveddi.testscommon.variables.GlobalVariables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dwiveddi on 2/6/2018.
 */
public class Response {
     private static final FreemarkerTemplateEngine engine = FreemarkerTemplateEngine.getInstance();

     @CsvMapped.Column(index = 6, converterMethod = "getInt") private int statusCode;
     @CsvMapped.Column(index = 7) private String headers;
     //@CsvMapped.Column(index = 7, converterMethod = "convertTopMap") private Map<String,String> headers = new HashMap<>();
     @CsvMapped.Column(index = 8) private String payload;
     @CsvMapped.Column(index = 9, converterMethod = "getBoolean")private boolean payloadJsonValdationRequired = false;
     @CsvMapped.Column(index = 10,converterMethod = "getPayloadStructure" )private PayloadStructure payloadStructure = PayloadStructure.JSON;
     @CsvMapped.Column(index = 11) private String jsonAttributes;

     public void format(Map<String, Object> map) {
          this.payload = engine.generate(this.payload, map);
          this.headers = engine.generate(this.headers, map);
     }

     public int getInt(String s){
          s = engine.generate(s, GlobalVariables.INSTANCE);
          return Integer.parseInt(s.trim());
     }
     public boolean getBoolean(String s){
          s = engine.generate(s, GlobalVariables.INSTANCE);
          return s.isEmpty() ? false : Boolean.parseBoolean(s.trim().toLowerCase());
     }

     public List<Object> getList(String s) {
          try {
               s = engine.generate(s, GlobalVariables.INSTANCE);
               return s.isEmpty() ? new ArrayList<>() : (List)JsonUtils.fromJson(s, List.class);
          }catch (Exception e){
               throw new RuntimeException("Exception while gettingList from String = "+s, e);
          }
     }
     public PayloadStructure getPayloadStructure(String s){
          return s.isEmpty() ? null : PayloadStructure.valueOf(s);
     }
/*   public Map<String, String> convertTopMap(String headers) throws IOException {
          headers = engine.generate(headers, FreemarkerTemplateEngine.getInstance().getGlobalMap());
          if(!headers.isEmpty()) {
               return new ObjectMapper().readValue(headers, Map.class);
          }
          return null;
     }*/

     @Override
     public String toString() {
          return "com.dwiveddi.restapi.dto.Response{" +
                  "statusCode=" + statusCode +
                  ", payload='" + payload + '\'' +
                  ", headers=" + headers +
                  ", payloadJsonValdationRequired=" + payloadJsonValdationRequired +
                  ", payloadStructure=" + payloadStructure +
                  ", jsonAttributes=" + jsonAttributes +
                  '}';
     }

     public int getStatusCode() {
          return statusCode;
     }

     public void setStatusCode(int statusCode) {
          this.statusCode = statusCode;
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

     public boolean isPayloadJsonValdationRequired() {
          return payloadJsonValdationRequired;
     }

     public void setPayloadJsonValdationRequired(boolean payloadJsonValdationRequired) {
          this.payloadJsonValdationRequired = payloadJsonValdationRequired;
     }

     public List<Object> getJsonAttributes() {
          return getList(this.jsonAttributes);
     }

     public String getXmlSchemaPath(){
         return this.jsonAttributes;
     }

    /* public void setJsonAttributes(List<Object> jsonAttributes) {
          this.jsonAttributes = jsonAttributes;
     }*/

     public PayloadStructure getPayloadStructure() {
          return payloadStructure;
     }

     public void setPayloadStructure(PayloadStructure payloadStructure) {
          this.payloadStructure = payloadStructure;
     }

}
