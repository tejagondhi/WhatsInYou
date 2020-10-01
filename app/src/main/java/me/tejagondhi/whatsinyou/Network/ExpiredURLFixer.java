package me.tejagondhi.whatsinyou.Network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import me.tejagondhi.whatsinyou.CallBacks.FacebookObjectReady;
import me.tejagondhi.whatsinyou.CallBacks.InstagramObjectReady;
import me.tejagondhi.whatsinyou.Data.DBHelper;
import me.tejagondhi.whatsinyou.Data.FeedDataObject;

public class ExpiredURLFixer {
    private final DBHelper dbHelper;
    Context context;

    public ExpiredURLFixer(Context context) {
        this.context = context;
        dbHelper = new DBHelper(context);
    }

    public void updateURLs(){
        ArrayList<FeedDataObject> data = dbHelper.getFeed();
        for (FeedDataObject row:data) {
            if(row.getSource().equalsIgnoreCase("Instagram")){
                new InstagramDownloader(row.getOriginalURL(),new UpdateCallback(row.getID()));
            }else if (row.getSource().equalsIgnoreCase("Facebook")){
                new FacebookDownloader(new UpdateCallback(row.getID()),row.getOriginalURL());
            }
        }

    }
    public class UpdateCallback implements InstagramObjectReady, FacebookObjectReady {
        String id;

        public UpdateCallback(String id) {
            this.id = id;
        }

        @Override
        public void onInstagramObjectReady(FeedDataObject instagram) {
            dbHelper.updateFeed(instagram,id);
        }

        @Override
        public void OnFacebookObjectReady(FeedDataObject facebookData) {
            dbHelper.updateFeed(facebookData,id);
        }
    }
}
