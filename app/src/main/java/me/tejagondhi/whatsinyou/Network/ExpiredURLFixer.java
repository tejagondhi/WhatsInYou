package me.tejagondhi.whatsinyou.Network;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import me.tejagondhi.whatsinyou.CallBacks.InstagramObjectReady;
import me.tejagondhi.whatsinyou.Data.DBHelper;
import me.tejagondhi.whatsinyou.Data.Instagram;

public class ExpiredURLFixer {
    Context context;
    HashMap<String,Instagram> updateList= new HashMap<>();
    DBHelper dbHelper =new DBHelper(context);
    public ExpiredURLFixer(Context context) {
        this.context = context;
    }

    public void updateURLs(){
        ArrayList<HashMap<String, String>> data = dbHelper.getFeed();
        for (HashMap<String, String> row:data) {
            new InstagramDownloader(row.get("ORIGINAL_URL"),new UpdateCallback(row.get("ID")));
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
        public void onInstagramObjectReady(Instagram instagram) {
            updateList.put(id,instagram);
        }
    }
}
