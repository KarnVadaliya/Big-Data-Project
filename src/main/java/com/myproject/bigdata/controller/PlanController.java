package com.myproject.bigdata.controller;

import com.myproject.bigdata.beans.EtagManager;
import com.myproject.bigdata.service.PlanService;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONTokener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.json.JSONObject;

import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;


@RestController
public class PlanController {

    PlanService planService = new PlanService();

    EtagManager etagManager = new EtagManager();

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, value = "/plan")
    public ResponseEntity createPlan(@RequestBody String jsonData) throws URISyntaxException {

        String responseBody,message;
        JSONObject jsonPlan = new JSONObject(new JSONTokener(jsonData));

        JSONObject jsonSchema = new JSONObject(new JSONTokener(PlanController.class.getResourceAsStream("/planSchema.json")));

        Schema planSchema = SchemaLoader.load(jsonSchema);

        try {
            planSchema.validate(jsonPlan);
        } catch (ValidationException e){
            e.getCausingExceptions().stream().map(ValidationException::getMessage).forEach(System.out::println);
            message = "Invalid Input! Error: "+e.getAllMessages();
            responseBody = "{\n" + "\t\"message\": \"" + message + "\"\n" + "}";
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseBody);
        }

        if(this.planService.checkPlanExists((String) jsonPlan.get("objectId"))){
            message = "Plan Already Exists !";
            responseBody = "{\n" + "\t\"message\": \"" + message + "\"\n" + "}";
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseBody);
        }

        String objectID = this.planService.savePlan(jsonPlan);

        JSONObject jsonObject = this.planService.getPlan(objectID);
        String etag = etagManager.getETag(jsonObject);

        message = "Plan Created Successfully!!";
        responseBody = "{\n" +
                "\t\"objectId\": \"" + objectID + "\"\n" +
                "\t\"message\": \"" + message + "\"\n" +
                "}";
        return ResponseEntity.created(new URI(jsonPlan.get("objectId").toString())).eTag(etag).body(responseBody);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/plan/{objectID}")
    public ResponseEntity getPlan(@PathVariable String objectID, @RequestHeader HttpHeaders requestHeaders){

        String message,responseBody;
        if(!this.planService.checkPlanExists(objectID)){
            message = "Plan Not Found !";
            responseBody = "{\n" + "\t\"message\": \"" + message + "\"\n" + "}";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject jsonObject = this.planService.getPlan(objectID);
        String etag = etagManager.getETag(jsonObject);
        headers.setETag(etag);

        if(!etagManager.verifyETag(jsonObject,requestHeaders.getIfNoneMatch()))
            return new ResponseEntity<>(jsonObject.toMap(), headers, HttpStatus.OK);
        else
            return new ResponseEntity<>(null, headers, HttpStatus.NOT_MODIFIED);

    }

    @RequestMapping(method =  RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/plan/{objectID}")
    public ResponseEntity deletePlan(@PathVariable String objectID){

        String message,responseBody;
        if(!this.planService.checkPlanExists(objectID)){
            message = "Plan Not Found !";
            responseBody = "{\n" + "\t\"message\": \"" + message + "\"\n" + "}";
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseBody);
        }

        this.planService.deletePlan(objectID);

        return ResponseEntity.noContent().build();
    }

}

