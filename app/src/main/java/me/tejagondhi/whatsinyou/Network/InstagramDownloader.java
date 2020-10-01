package me.tejagondhi.whatsinyou.Network;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONException;
import org.json.JSONObject;

import me.tejagondhi.whatsinyou.CallBacks.InstagramObjectReady;
import me.tejagondhi.whatsinyou.Data.Instagram;

public class InstagramDownloader  {

    private final InstagramObjectReady callback;
    private String url;
    JSONObject response=null;
    private Instagram insta;

    public InstagramDownloader(String url, InstagramObjectReady callback) {
       this.url=url;
       this.callback=callback;
       getImageJson();
    }

    public JSONObject getImageJson(){
        AndroidNetworking.get(url)
                .setTag("test")
                .setPriority(Priority.LOW)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // do anything with response
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
        return response;
    }

    private void saveInstaObject(JSONObject response) throws JSONException {
        insta=new Instagram();
        insta.setOriginalURL(url);
        if (response.getJSONObject("graphql").getJSONObject("shortcode_media").getBoolean("is_video")) {
            insta.setUrl(response.getJSONObject("graphql").getJSONObject("shortcode_media").getString("video_url"));
            insta.setName(response.getJSONObject("graphql").getJSONObject("shortcode_media").getString("id"));
            insta.setExtension(".mp4");
            insta.setIsVideo(true);
            insta.setHeight(response.getJSONObject("graphql").getJSONObject("shortcode_media").getJSONObject("dimensions").getString("height"));
            insta.setWidth(response.getJSONObject("graphql").getJSONObject("shortcode_media").getJSONObject("dimensions").getString("width"));
            callback.onInstagramObjectReady(insta);
        }else {
            insta.setUrl(response.getJSONObject("graphql").getJSONObject("shortcode_media").getString("display_url"));
            insta.setName(response.getJSONObject("graphql").getJSONObject("shortcode_media").getString("id"));
            insta.setExtension(".jpg");
            insta.setIsVideo(false);
            insta.setHeight(response.getJSONObject("graphql").getJSONObject("shortcode_media").getJSONObject("dimensions").getString("height"));
            insta.setWidth(response.getJSONObject("graphql").getJSONObject("shortcode_media").getJSONObject("dimensions").getString("width"));
            callback.onInstagramObjectReady(insta);
        }
    }


}
