package com.muse.api;

import android.app.Activity;
import android.util.Log;

import com.muse.api.finder.ActivityFinder;
import com.muse.api.finder.Finder;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by GuoWee on 2018/3/15.
 */

public class InjectHelper {
    private static final String SUFFIX = "$$ViewInjector";
    private static final ActivityFinder ACTIVITY_FINDER = new ActivityFinder();
    private static final Map<String, Injector> FINDER_MAP = new HashMap<>();

    public static void inject(Activity host) {
        inject(host, host, ACTIVITY_FINDER);
    }

    public static void inject(Object host, Object source, Finder finder) {
        String className = host.getClass().getName();
        try {
            Injector injector = FINDER_MAP.get(className);
            if (injector == null) {
                String classFullName = host.getClass().getName() + SUFFIX;
                Class<?> finderClass = Class.forName(classFullName);
                injector = (Injector) finderClass.newInstance();
                FINDER_MAP.put(className, injector);
            }
            injector.inject(host, source, finder);
        } catch (Exception e) {
            throw new RuntimeException("Unable to inject for " + className, e);
        }
    }


}
