package com.myproject.bigdata.controller;

import com.myproject.bigdata.beans.EtagManager;
import com.myproject.bigdata.exception.InvalidInputException;
import com.myproject.bigdata.exception.PlanAlreadyPresentException;
import com.myproject.bigdata.exception.PlanNotFoundException;
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

        JSONObject jsonPlan = new JSONObject(new JSONTokener(jsonData));

        JSONObject jsonSchema = new JSONObject(new JSONTokener(PlanController.class.getResourceAsStream("/planSchema.json")));

        Schema planSchema = SchemaLoader.load(jsonSchema);

        try {
            planSchema.validate(jsonPlan);
        } catch (ValidationException e){
            e.getCausingExceptions().stream().map(ValidationException::getMessage).forEach(System.out::println);
            throw new InvalidInputException("Invalid Input! Error: " + e.getMessage());

        }

        if(this.planService.checkIfPlanExists((String) jsonPlan.get("objectId"))){
            throw new PlanAlreadyPresentException("Plan has already present!!");
        }

        String objectID = this.planService.savePlan(jsonPlan);

        JSONObject jsonObject = this.planService.getPlan(objectID);
        String etag = etagManager.getETag(jsonObject);

        String message = "Plan Created Successfully!!";
        String responseBody = "{\n" +
                "\t\"objectId\": \"" + objectID + "\"\n" +
                "\t\"message\": \"" + message + "\"\n" +
                "}";
        return ResponseEntity.created(new URI(jsonPlan.get("objectId").toString())).eTag(etag).body(responseBody);
    }

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, value = "/plan/{objectID}")
    public ResponseEntity getPlan(@PathVariable String objectID, @RequestHeader HttpHeaders requestHeaders){

        if(!this.planService.checkIfPlanExists(objectID)){
            throw new PlanNotFoundException("Plan not found!!");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject jsonObject = this.planService.getPlan(objectID);
        String etag = etagManager.getETag(jsonObject);
        headers.setETag(etag);


        if(!etagManager.verifyETag(jsonObject,requestHeaders.getIfNoneMatch()))
            return new ResponseEntity<>(jsonObject.toMap(), headers, HttpStatus.OK);
        else
            return new ResponseEntity<>("", headers, HttpStatus.NOT_MODIFIED);


       // return ResponseEntity.ok().body(jsonObject.toString());
    }

    @RequestMapping(method =  RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE, value = "/plan/{objectID}")
    public ResponseEntity deletePlan(@PathVariable String objectID){

        if(!this.planService.checkIfPlanExists(objectID)){
            throw new PlanNotFoundException("Plan not found!!");
        }
        JSONObject jsonObject = this.planService.getPlan(objectID);
        String etag = etagManager.getETag(jsonObject);

        this.planService.deletePlan(objectID);

        return ResponseEntity.noContent().eTag(etag).build();
    }

}

