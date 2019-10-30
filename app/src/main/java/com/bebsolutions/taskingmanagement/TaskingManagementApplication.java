package com.bebsolutions.taskingmanagement;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

public class TaskingManagementApplication extends MultiDexApplication {


    @Override
    public void onCreate() {
        MultiDex.install(getApplicationContext());
        super.onCreate();
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
