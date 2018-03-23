package com.muse.api;

import com.muse.api.finder.Finder;

/**
 * Created by GuoWee on 2018/3/23.
 */

public interface Injector<T> {
    void inject(T host, Object source, Finder finder);
}
