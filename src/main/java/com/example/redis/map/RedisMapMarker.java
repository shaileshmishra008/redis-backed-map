package com.example.redis.map;

import java.io.Serializable;

public final class RedisMapMarker implements Serializable {

    String redisKey;

    public RedisMapMarker(){

    }

    public RedisMapMarker(String key){
          redisKey = key;
    }

    public String getKey(){
        return redisKey;
    }


}
