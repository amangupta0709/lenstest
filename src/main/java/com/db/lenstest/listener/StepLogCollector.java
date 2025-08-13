package com.db.lenstest.listener;

import java.util.ArrayList;
import java.util.List;

public class StepLogCollector {
    private static final ThreadLocal<List<String>> logCollector = ThreadLocal.withInitial(ArrayList::new);

    public static void add(String value) { logCollector.get().add(value); }

    public static List<String> get() { return logCollector.get(); }

    public static void clear() { logCollector.get().clear(); }
}
