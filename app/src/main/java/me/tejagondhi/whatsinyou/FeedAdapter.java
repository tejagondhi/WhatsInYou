package me.tejagondhi.whatsinyou;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import me.tejagondhi.whatsinyou.CallBacks.DownloadComplete;
import me.tejagondhi.whatsinyou.CustomViews.FeedVideoView;
import me.tejagondhi.whatsinyou.Network.Downloader;
import me.tejagondhi.whatsinyou.Network.ExpiredURLFixer;

public class FeedAdapter extends ArrayAdapter<String> implements DownloadComplete {

    private final MainActivity context;
    private final ArrayList<HashMap<String, String>> feedData;


    public FeedAdapter(@NonNull MainActivity context, ArrayList<HashMap<String,String>> feedData) {
        super(context, R.layout.feed_listview);
        this.context=context;
        this.feedData = feedData;
    }

    @Override
    public int getCount() {
        return feedData.size();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.feed_listview, null,true);

        final ImageView imageView = (ImageView) rowView.findViewById(R.id.listImageView);
        final FeedVideoView videoView = (FeedVideoView) rowView.findViewById(R.id.listVideoView);
        final ImageView videoPlayButton = (ImageView)rowView.findViewById(R.id.videoPlayButton);
        ImageView download= rowView.findViewById(R.id.downloadImage);
        final ImageView share= rowView.findViewById(R.id.shareImage);

        if(feedData.get(position).get("TYPE").equalsIgnoreCase("image")){
            videoView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            videoPlayButton.setVisibility(View.GONE);
            Picasso.get().load(feedData.get(position).get("URL")).into(imageView);
        }else {
            videoView.setVideoSize(Integer.parseInt(feedData.get(position).get("WIDTH")),Integer.parseInt(feedData.get(position).get("HEIGHT")));
            videoView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            videoView.setVideoPath(feedData.get(position).get("URL"));
            videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                        AlertDialog.Builder errorDialog = new AlertDialog.Builder(context);
                        errorDialog.setMessage("URL expired, do you want to refresh URL?");
                        errorDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new ExpiredURLFixer(context).updateURLs();
                            }
                        });
                        errorDialog.setNegativeButton("Cancel",null);
                        errorDialog.show();
                    return true;
                }
            });
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    videoPlayButton.setVisibility(View.VISIBLE);
                }
            });
        }
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoView.isPlaying()){
                    videoView.pause();
                    videoPlayButton.setVisibility(View.VISIBLE);
                }else {
                    videoView.start();
                    videoPlayButton.setVisibility(View.INVISIBLE);
                }
            }
        });
        download.setOnClickListener(new ActionButtonListener(position));
        share.setOnClickListener(new ActionButtonListener(position));
        return rowView;
    }

    public void download(int position, FeedAdapter feedAdapter){
        Downloader downloader =new Downloader();
        if(feedData.get(position).get("TYPE").equalsIgnoreCase("image")){
            downloader.downloadRequest(feedData.get(position).get("URL"),feedData.get(position).get("NAME"),"jpg",context,feedAdapter,position);
        }else {
            downloader.downloadRequest(feedData.get(position).get("URL"),feedData.get(position).get("NAME"),"mp4",context,feedAdapter,position);
        }
    }

    public void share(int position){
        String path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File file = null;
        Intent share = new Intent(Intent.ACTION_SEND);
        if(feedData.get(position).get("TYPE").equalsIgnoreCase("IMAGE")){
            share.setType("image/jpg");
            file =new File(path,feedData.get(position).get("NAME")+".jpg");
        }else {
            share.setType("video/mp4");
            file =new File(path,feedData.get(position).get("NAME")+".mp4");
        }
        share.putExtra(Intent.EXTRA_STREAM,Uri.parse(file.toString()));
        context.startActivity(Intent.createChooser(share, "Share with"));
    }

    @Override
    public void onDownloadComplete(int position) {
        share(position);
    }

    public class ActionButtonListener implements View.OnClickListener {
        int position;

        public ActionButtonListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.downloadImage:
                        download(position, null);
                    break;
                case R.id.shareImage:
                    download(position,FeedAdapter.this);
                    break;
            }
        }
    }
}
