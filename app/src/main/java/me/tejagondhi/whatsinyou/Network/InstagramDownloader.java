package me.tejagondhi.whatsinyou.Network;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import me.tejagondhi.whatsinyou.CallBacks.InstagramObjectReady;
import me.tejagondhi.whatsinyou.Data.FeedDataObject;

public class InstagramDownloader  {

    private final InstagramObjectReady callback;
    private String url;

    public InstagramDownloader(String url, InstagramObjectReady callback) {
       this.url=url;
       this.callback=callback;
       getImageJson();
    }

    public void getImageJson(){
        AndroidNetworking.get(url)
                .setTag("INSTA")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            saveInstaObject(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                    }
                });
    }

    private void saveInstaObject(JSONObject response) throws JSONException {
        FeedDataObject dataObject = new FeedDataObject();
        dataObject.setOriginalURL(url);
        if (response.getJSONObject("graphql").getJSONObject("shortcode_media").getBoolean("is_video")) {
            dataObject.setUrl(response.getJSONObject("graphql").getJSONObject("shortcode_media").getString("video_url"));
            dataObject.setExtension(".mp4");
            dataObject.setIsVideo(true);
        }else {
            dataObject.setUrl(response.getJSONObject("graphql").getJSONObject("shortcode_media").getString("display_url"));
            dataObject.setExtension(".jpg");
            dataObject.setIsVideo(false);
        }
        dataObject.setName(response.getJSONObject("graphql").getJSONObject("shortcode_media").getString("id"));
        dataObject.setHeight(response.getJSONObject("graphql").getJSONObject("shortcode_media").getJSONObject("dimensions").getString("height"));
        dataObject.setWidth(response.getJSONObject("graphql").getJSONObject("shortcode_media").getJSONObject("dimensions").getString("width"));
        callback.onInstagramObjectReady(dataObject);
    }
}
