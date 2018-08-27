package core.framework.impl.cache;

import core.framework.cache.Cache;
import core.framework.impl.json.JSONReader;
import core.framework.impl.json.JSONWriter;
import core.framework.util.Maps;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author neo
 */
public class CacheImpl<T> implements Cache<T> {
    public final String name;
    public final Type valueType;
    public final Duration duration;
    private final CacheStore cacheStore;
    private final JSONReader<T> reader;
    private final JSONWriter<T> writer;

    CacheImpl(String name, Type valueType, Duration duration, CacheStore cacheStore) {
        this.name = name;
        this.valueType = valueType;
        this.duration = duration;
        this.cacheStore = cacheStore;
        reader = JSONReader.of(valueType);
        writer = JSONWriter.of(valueType);
    }

    @Override
    public T get(String key, Function<String, T> loader) {
        String cacheKey = cacheKey(key);
        byte[] cacheValue = cacheStore.get(cacheKey);
        if (cacheValue == null) {
            T value = loader.apply(key);
            cacheStore.put(cacheKey, writer.toJSON(value), duration);
            return value;
        }
        return reader.fromJSON(cacheValue);
    }

    public Optional<String> get(String key) {
        byte[] result = cacheStore.get(cacheKey(key));
        if (result == null) return Optional.empty();
        return Optional.of(new String(result, StandardCharsets.UTF_8));
    }

    @Override
    public Map<String, T> getAll(List<String> keys, Function<String, T> loader) {
        int size = keys.size();
        String[] cacheKeys = new String[size];
        int index = 0;
        for (String key : keys) {
            cacheKeys[index] = cacheKey(key);
            index++;
        }
        Map<String, T> values = Maps.newLinkedHashMapWithExpectedSize(size);
        Map<String, byte[]> newValues = Maps.newHashMapWithExpectedSize(size);
        Map<String, byte[]> cacheValues = cacheStore.getAll(cacheKeys);
        index = 0;
        for (String key : keys) {
            String cacheKey = cacheKeys[index];
            byte[] cacheValue = cacheValues.get(cacheKey);
            if (cacheValue == null) {
                T value = loader.apply(key);
                newValues.put(cacheKey, writer.toJSON(value));
                values.put(key, value);
            } else {
                values.put(key, reader.fromJSON(cacheValue));
            }
            index++;
        }
        if (!newValues.isEmpty()) cacheStore.putAll(newValues, duration);
        return values;
    }

    @Override
    public void put(String key, T value) {
        cacheStore.put(cacheKey(key), writer.toJSON(value), duration);
    }

    @Override
    public void putAll(Map<String, T> values) {
        Map<String, byte[]> cacheValues = Maps.newHashMapWithExpectedSize(values.size());
        for (Map.Entry<String, T> entry : values.entrySet()) {
            cacheValues.put(cacheKey(entry.getKey()), writer.toJSON(entry.getValue()));
        }
        cacheStore.putAll(cacheValues, duration);
    }

    @Override
    public void evict(String... keys) {
        if (keys.length == 0) throw new Error("keys must not be empty");
        String[] cacheKeys = new String[keys.length];
        for (int i = 0; i < keys.length; i++) {
            cacheKeys[i] = cacheKey(keys[i]);
        }
        cacheStore.delete(cacheKeys);
    }

    private String cacheKey(String key) {
        return name + ":" + key;
    }
}
