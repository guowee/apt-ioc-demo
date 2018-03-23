package com.muse.api.finder;

import android.app.Activity;
import android.view.View;

/**
 * Created by GuoWee on 2018/3/23.
 */

public class ActivityFinder implements Finder {

    @Override
    public View findView(Object source, int id) {
        return ((Activity)source).findViewById(id);
    }
}
