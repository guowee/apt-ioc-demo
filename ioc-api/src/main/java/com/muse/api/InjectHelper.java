package com.muse.api;

import android.app.Activity;
import android.util.Log;

import java.lang.reflect.Constructor;

/**
 * Created by GuoWee on 2018/3/15.
 */

public class InjectHelper {
    private static final String SUFFIX = "$$ViewInjector";

    public static void inject(Activity host) {
        String classFullName = host.getClass().getName() + SUFFIX;

        try {
            Class proxy = Class.forName(classFullName);
            Constructor constructor = proxy.getConstructor(host.getClass());
            constructor.newInstance(host);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
