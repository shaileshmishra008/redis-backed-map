package com.example.redis.map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.Map;

public class CustomMapSerializer  extends GenericJackson2JsonRedisSerializer /*JdkSerializationRedisSerializer*/ {

    private HashOperations hashOperations;


    public CustomMapSerializer(HashOperations hashOperations){
        //NOTE: DO NOT USE OBJECT MAPPER WHICH WRITES CLASS NAMES (using mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");) TO JSON AS DEFAULT IS MAP WHICH IS WHAT WE ARE USING HERE!!
           super(new ObjectMapper());
           this.hashOperations = hashOperations;
    }
    @Override
    public byte[] serialize(Object o) throws SerializationException {
        if(o instanceof CacheableMap){
           //rather than writing map as object. create HSET in redis.
            //TODO: how to figure out the key?? --
            CacheableMap map = (CacheableMap)o;
            this.hashOperations.putAll(map.getCacheKey(), map);
            return super.serialize(new RedisMapMarker(map.getCacheKey()));
        }
        return super.serialize(o);
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        Object o= super.deserialize(bytes);
        if(o instanceof RedisMapMarker){
            RedisMapMarker marker = (RedisMapMarker)o;
            String key = marker.getKey();
            Map data = this.hashOperations.entries(key);
            CacheableMap cacheableChildMap = new CacheableMap(key, this.hashOperations);
            cacheableChildMap.putAllLocal(data);
            return cacheableChildMap;

        }
        return o;
    }
}
