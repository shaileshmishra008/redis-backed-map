package com.example.redis.map;

import org.springframework.data.redis.core.HashOperations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CacheableMap<K,V> extends HashMap<K,V> {

    public String getCacheKey() {
        return cacheKey;
    }

    private String cacheKey;
    //TODO: should this be configurable
    private Long refreshInterval = -1L;
    transient HashOperations hashOperations;

    private AtomicLong lastRefreshed;


    public CacheableMap(String cacheKey, HashOperations hashOperations){
        this.cacheKey = cacheKey;
        this.hashOperations = hashOperations;
        this.lastRefreshed = new AtomicLong(System.currentTimeMillis());
    }

    @Override
    public V get(Object key) {
        V value;
        if(isRefreshNeeded()){
            value = (V)this.hashOperations.get(this.cacheKey, key);//updateFromCache(); //TODO: if refreshInterval is <= 0, we can not fetch complete map every time.
            //store the latest value from redis to in memory map
            super.put((K)key, value);
            return value;
        }

        return super.get(key);
    }

    @Override
    public V put(K key, V value){
        hashOperations.put(cacheKey, key, value);
           super.put(key, value);
           return value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m){
        this.hashOperations.putAll(this.cacheKey, m);
        super.putAll(m);

    }

    @Override
    public Set<Entry<K,V>> entrySet() {
        if(isRefreshNeeded()){
            updateFromCache();
        }
        return super.entrySet();
    }

    void putAllLocal(Map<? extends K, ? extends V> m){
        super.putAll(m);

    }

    /**
     * TODO: We must have timeouts on Redis Ops and a mechanism to retry/back off in case of failures
     */
    private synchronized void updateFromCache(){
        //check if some other thread has updated the cache while we were waiting for lock.
        if(isRefreshNeeded()) {
            Map data = this.hashOperations.entries(this.cacheKey);
            super.putAll(data);
            this.updateRefreshed();
        }
    }
    private boolean isRefreshNeeded(){
        return (this.refreshInterval <= 0) ? true : (System.currentTimeMillis() - this.lastRefreshed.longValue()) >= this.refreshInterval;
    }

    private void updateRefreshed(){
        this.lastRefreshed.getAndSet(System.currentTimeMillis());
    }

    @Override
    public int size() {
       return this.hashOperations.size(this.cacheKey).intValue();
    }

    @Override
    public boolean isEmpty() {
        return this.hashOperations.size(this.cacheKey).intValue() > 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.hashOperations.hasKey(this.cacheKey, key);
    }

    @Override
    public V remove(Object key) {
        /*
           Since hashOp does not return value for key being deleted. We end-up making two calls
         */
        get(key);
        this.hashOperations.delete(this.cacheKey, key);
        return super.remove(key);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<V> values() {
        return this.hashOperations.values(this.cacheKey);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return (V)this.hashOperations.putIfAbsent(this.cacheKey, key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V replace(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException();
    }

}
