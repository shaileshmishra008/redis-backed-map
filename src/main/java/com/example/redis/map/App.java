package com.example.redis.map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.data.redis.connection.lettuce.DefaultLettucePool;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

/**
 * Example code demonstrate usages!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {


       /*
        System.out.println( "Spring Redis Cache Demo" );
        App app = new App();
        RedisTemplate redisTemplate = app.redisTemplate();
        ValueOperations redis = redisTemplate.opsForValue();
       for(int i = 0; i < 5 ; i++) {
           redis.set("demo-key-"+i, new DemoBean("demo"+i, 35, Arrays.asList("bigdata", "ml", "analytics")));
       }

        DemoBean data = (DemoBean)redis.get("demo-key-1");

        System.out.println("data from cache - : "+data);

        Collection keys = new ArrayList();

        for(int i = 0; i < 5 ; i++) {
            keys.add("demo-key-"+i);
        }

        List dataObjs = redis.multiGet(keys);

        for(Object d : dataObjs){
            System.out.println(d);
        } */
        /*
           saving a map object and retrieving complete map OR a single key from the map.
           NOTE: we can't set expiry for individual entries in the map.
         */

        Map childMap = MapFactory.getInstance().getMapByKey("map-in-map");
        Map childChildMap = MapFactory.getInstance().getMapByKey("map-in-map-in-map");
        childChildMap.put("child-1-1", "xyz");
        childMap.put("child-1", "abc");
        childMap.put("child-2", 12345L);
        childMap.put("child-3", childChildMap);
        Map<String, Object> map = MapFactory.getInstance().getMapByKey("root-map");
        map.put("1", "string-val");
        map.put("2", 12345L);
        map.put("3", new DemoBean("demo-hash", 35, Arrays.asList("bigdata", "ml", "analytics")));
        map.put("4", childMap);

        Map child = (Map)map.get("4");
        child.put("child-4", "child-4-val");

        System.out.println(child);

        CopyOnWriteArrayList<Long> itAccu = new CopyOnWriteArrayList<>();
        Runnable r1 = () -> {
        IntStream.range(0, 1000).forEach(
                n -> {
                    Long t1 = System.currentTimeMillis();
                    Iterator it = childMap.entrySet().iterator();
                    while(it.hasNext()){
                        it.next();
                    }

                    Long t2 = System.currentTimeMillis();
                    itAccu.add(t2-t1);
                    System.out.println("Iteration ->"+n);
                }
        );
    };
        CopyOnWriteArrayList<Long> putAccu = new CopyOnWriteArrayList<>();
        Runnable r2 = () -> {
            IntStream.range(0, 1000).forEach(
                    n -> {
                        Long t1 = System.currentTimeMillis();
                        childChildMap.put(""+n, n);
                        Long t2 = System.currentTimeMillis();
                        putAccu.add(t2-t1);
                        System.out.println("Put ->"+n);
                    }
            );
        };
        CopyOnWriteArrayList<Long> getAccu = new CopyOnWriteArrayList<>();
        Runnable r3 = () -> {
            Random rand = new Random();
            IntStream.range(0, 1000).forEach(
                    n -> {
                        Long t1 = System.currentTimeMillis();
                        childMap.get("child-"+rand.nextInt(3));
                        Long t2 = System.currentTimeMillis();
                        getAccu.add(t2-t1);
                        System.out.println("Get ->"+n);
                    }
            );
        };

        Thread t1 = new Thread(r1);
        Thread t2 = new Thread(r2);
        Thread t3 = new Thread(r3);

        t1.start();t2.start();t3.start();
        t1.join();
        t2.join();
        t3.join();

        IntStream.range(0, 1000).forEach(
                n -> {
                    System.out.println(" -> "+childChildMap.get(""+n));
                }
        );


        Long totalItTime = itAccu.stream().mapToLong(i -> i).sum();
        System.out.println("Avg it time in microsec : "+totalItTime*1000/itAccu.size() );

        Long totalPutTime = putAccu.stream().mapToLong(i -> i).sum();
        System.out.println("Avg put time in microsec : "+totalPutTime*1000/putAccu.size() );

        Long totalGetTime = getAccu.stream().mapToLong(i -> i).sum();
        System.out.println("Avg get time in microsec : "+totalGetTime*1000/getAccu.size() );

        /*
           PRESS CTRL + C to terminate the main.
         */

    }
}
