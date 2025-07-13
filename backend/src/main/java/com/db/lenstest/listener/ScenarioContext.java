package com.db.lenstest.listener;

import java.util.HashMap;
import java.util.Map;

public class ScenarioContext {
    private static final ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(HashMap::new);

    public static void put(String key, Object value){
        context.get().put(key, value);
    }

    public static <T> T get(String key, Class<T> tClass){
        return tClass.cast(context.get().get(key));
    }

    public static Object get(String key){
        return context.get().get(key);
    }

    public static void clear(){
        context.get().clear();
    }

}
