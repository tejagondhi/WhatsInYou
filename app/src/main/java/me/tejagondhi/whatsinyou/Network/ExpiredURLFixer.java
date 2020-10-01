package me.tejagondhi.whatsinyou.Network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import me.tejagondhi.whatsinyou.CallBacks.InstagramObjectReady;
import me.tejagondhi.whatsinyou.Data.DBHelper;
import me.tejagondhi.whatsinyou.Data.FeedDataObject;

public class ExpiredURLFixer {
    Context context;
    HashMap<String, FeedDataObject> updateList= new HashMap<>();
    DBHelper dbHelper =new DBHelper(context);
    public ExpiredURLFixer(Context context) {
        this.context = context;
    }

    public void updateURLs(){
        ArrayList<FeedDataObject> data = dbHelper.getFeed();
        for (FeedDataObject row:data) {
            new InstagramDownloader(row.getOriginalURL(),new UpdateCallback(row.getID()));
        }
        if(updateList.size()>0){
            dbHelper.updateFeed(updateList);
        }
    }
    public class UpdateCallback implements InstagramObjectReady {
        String id;

        public UpdateCallback(String id) {
            this.id = id;
        }

        @Override
        public void onInstagramObjectReady(FeedDataObject instagram) {
            updateList.put(id,instagram);
        }
    }
}
