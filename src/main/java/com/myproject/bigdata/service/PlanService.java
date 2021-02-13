package com.myproject.bigdata.service;

import com.myproject.bigdata.controller.PlanController;
import com.myproject.bigdata.exception.InvalidInputException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

public class PlanService {

    private JedisPool jedisPool;

    private JedisPool getJedisPool() {
        if (this.jedisPool == null) {
            this.jedisPool = new JedisPool();
        }
        return this.jedisPool;
    }


    public boolean checkIfPlanExists(String objectKey){

        Jedis jedis = this.getJedisPool().getResource();
        String jsonString = jedis.get(objectKey);
        jedis.close();
        if (jsonString == null || jsonString.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public String savePlan(JSONObject jsonObject){

        // Save the Object in Redis
        String objectKey = (String) jsonObject.get("objectId");
        Jedis jedis = this.getJedisPool().getResource();
        jedis.set(objectKey, jsonObject.toString());
        jedis.close();

        return objectKey;

    }

    public JSONObject getPlan(String objectKey) {
        JedisPool jedisPool = new JedisPool();
        Jedis jedis =  jedisPool.getResource();

        String jsonString = jedis.get(objectKey);
        jedis.close();
        if (jsonString == null || jsonString.isEmpty()) {
            return null;
        }
        JSONObject jsonObject = new JSONObject(jsonString);

        return  jsonObject;
    }

    public boolean deletePlan(String objectKey){

        Jedis jedis =  this.getJedisPool().getResource();
        jedis.del(objectKey);
        jedis.close();

        return true;

    }

}

