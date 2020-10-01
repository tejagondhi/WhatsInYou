package me.tejagondhi.whatsinyou;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;
import com.downloader.PRDownloader;
import com.jacksonandroidnetworking.JacksonParserFactory;

public class AppClass extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PRDownloader.initialize(getApplicationContext());
        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.setParserFactory(new JacksonParserFactory());

    }
}
