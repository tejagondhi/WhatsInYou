package me.tejagondhi.whatsinyou.Network;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.tejagondhi.whatsinyou.CallBacks.FacebookObjectReady;
import me.tejagondhi.whatsinyou.Data.FeedDataObject;

public class FacebookDownloader {
    public final FacebookObjectReady callback;

    public FacebookDownloader(FacebookObjectReady callback, String url) {
        this.callback=callback;
        FacebookUrlDigger task= new FacebookUrlDigger(callback,url);
        task.execute(url);
    }

    public static class FacebookUrlDigger extends AsyncTask<String, Void, String> {

        private final FacebookObjectReady callback;
        private final String url;

        public FacebookUrlDigger(FacebookObjectReady callback, String url) {
            this.callback=callback;
            this.url=url;
        }

        @Override
        public String doInBackground(String... params) {
            StringBuilder str = null;
            try {
                URL url = new URL(params[0]);
                HttpURLConnection response = (HttpURLConnection) url.openConnection();
                InputStream in = response.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                str = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    str.append(line);
                }
                in.close();
            }catch (Exception e){
                e.printStackTrace();
                return "";
            }
            if (str != null) {
                return str.toString();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String html) {
            super.onPostExecute(html);
            getHtmlData(html,url);
        }

        public void getHtmlData(String html, String url){
            Pattern videoPattern=Pattern.compile("contentUrl\":\"(.*)\",\"int");
            Pattern heightPattern=Pattern.compile("(?<=og:video:height\" content=\").*?(?=\" />)");
            Pattern widthPattern=Pattern.compile("(?<=og:video:width\" content=\").*?(?=\" />)");
            Pattern namePattern=Pattern.compile("[^/\\\\&\\?]+\\.\\w{3,4}(?=([\\?&].*$|$))");

            Matcher videoMatcher=videoPattern.matcher(html);
            Matcher heightMatcher=heightPattern.matcher(html);
            Matcher widthMatcher=widthPattern.matcher(html);

            if(videoMatcher.find()&&heightMatcher.find()&&widthMatcher.find()){

                String videoUrl= Objects.requireNonNull(videoMatcher.group(1)).replace("\\","");
                Matcher nameMatcher=namePattern.matcher(videoUrl);

                FeedDataObject dataObject=new FeedDataObject();
                dataObject.setIsVideo(true);
                //dataObject.setWidth(html.substring(widthMatcher.start(),widthMatcher.end()));
                //dataObject.setHeight(html.substring(heightMatcher.start(),heightMatcher.end()));
                dataObject.setWidth("720");
                dataObject.setHeight("720");
                dataObject.setUrl(videoUrl);
                dataObject.setSource("Facebook");
                dataObject.setOriginalURL(url);
                if(nameMatcher.find()){
                    dataObject.setName(videoUrl.substring(nameMatcher.start(),nameMatcher.end()).replace(".mp4",""));
                }else{
                    //keep random Name
                    dataObject.setName(Math.random()+new Date().toString());
                }
                callback.OnFacebookObjectReady(dataObject);
            }else{
                //fucked up send msg to developer

            }
        }
    }
}
