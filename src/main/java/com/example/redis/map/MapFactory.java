package com.example.redis.map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.data.redis.connection.lettuce.DefaultLettucePool;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Map;

public class MapFactory {

    private static final MapFactory _fac = new MapFactory();

    private RedisTemplate _template;

    private MapFactory(){

        this._template = redisTemplate();

    }

    public static MapFactory getInstance() {
        return _fac;
    }


    public Map getMapByKey(String key){
        assert key != null;

        return new CacheableMap<>(key, this._template.opsForHash());
    }

    private LettuceConnectionFactory redisConnectionFactory() {

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMinIdle(5);
        DefaultLettucePool pool = new DefaultLettucePool("localhost", 6379, poolConfig);
        pool.afterPropertiesSet();
        LettuceConnectionFactory fac = new LettuceConnectionFactory(pool);
        fac.setShareNativeConnection(true);
        fac.afterPropertiesSet();
        return fac;
    }

    private RedisTemplate redisTemplate() {
        ObjectMapper mapper = new ObjectMapper();
        //mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
        //mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
        /*
          THIS IS NEEDED TO ENCODE CLASS NAME IN SERIALIZED JSON  SO THAT IT CAN BE DESERIALIZED BACK TO SAME OBJECT
          GenericJackson2JsonRedisSerializer internally uses this property when initialized with an string for holding fqcn for bean
         */
        mapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL, "@class");
        RedisTemplate template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer((new GenericJackson2JsonRedisSerializer("@class")));//new JdkSerializationRedisSerializer());
        template.setEnableDefaultSerializer(false);
        //template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new CustomMapSerializer(template.opsForHash()));
        // explicitly enable transaction support
        //template.setEnableTransactionSupport(true);
        template.afterPropertiesSet();
        return template;
    }

}
