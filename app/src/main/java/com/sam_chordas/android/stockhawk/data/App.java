package com.sam_chordas.android.stockhawk.data;

import android.app.Application;

        import com.squareup.otto.Bus;
        import com.squareup.otto.ThreadEnforcer;

/**

 This custom class is  Necessary since it starts EventBus when the App starts
 and allows to retrieve whatever EventBus is present with minimal code and
 reduced risk of error.
 **/
public class App extends Application {

    private static App sInstance;

    private Bus bus;

    public static App getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        bus = new Bus(ThreadEnforcer.ANY);
    }

    public Bus getEventBus() {
        return bus;
    }

}