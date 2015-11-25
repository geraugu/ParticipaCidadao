package com.monitorabrasil.participacidadao.application;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.monitorabrasil.participacidadao.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.parse.Parse;
import com.parse.ParseInstallation;
//import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;

import io.fabric.sdk.android.Fabric;

//import com.parse.ParseTwitterUtils;

/**
 * Created by geraugu on 6/10/15.
 */
public class MyApp extends Application {

    private static MyApp mInstance;
    private ImageLoader mImagemLoader;
    public static final String URL_FOTO = "http://52.27.220.189/parse/data/fotos/";


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
// Enable Local Datastore.

        Parse.enableLocalDatastore(this);
        Parse.initialize(this, AppConfig.PARSE_APPLICATION_ID, AppConfig.PARSE_CLIENT_KEY);
     //   ParseTwitterUtils.initialize(AppConfig.TWITTER_CONSUMER_KEY, AppConfig.TWITTER_CONSUMER_SECRET);
        ParseInstallation installation =  ParseInstallation.getCurrentInstallation();
        if(null != ParseUser.getCurrentUser())
            installation.put("user", ParseUser.getCurrentUser());
        installation.saveInBackground();
        mInstance = this;

        //configurando o imageloader
        DisplayImageOptions mDisplayImageOptions = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .showImageOnLoading(R.drawable.tw__ic_tweet_photo_error_light)
                .build();
        ImageLoaderConfiguration conf = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(mDisplayImageOptions)
                .memoryCacheSize(50*1024*1024)
                .build();
        this.mImagemLoader = ImageLoader.getInstance();
        mImagemLoader.init(conf);

    }

    public static synchronized MyApp getInstance() {
        return mInstance;
    }

    public ImageLoader getmImagemLoader() {
        return mImagemLoader;
    }

    public String getCidade(){
        return "Ouro Branco - MG";
    }
}
