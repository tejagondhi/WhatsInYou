package me.tejagondhi.whatsinyou;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import me.tejagondhi.whatsinyou.CallBacks.DownloadComplete;
import me.tejagondhi.whatsinyou.CustomViews.FeedVideoView;
import me.tejagondhi.whatsinyou.Data.FeedDataObject;
import me.tejagondhi.whatsinyou.Network.Downloader;
import me.tejagondhi.whatsinyou.Network.ExpiredURLFixer;

public class FeedAdapter extends ArrayAdapter<String> implements DownloadComplete {

    private final MainActivity context;
    private final ArrayList<FeedDataObject> feedData;


    public FeedAdapter(@NonNull MainActivity context,@NonNull ArrayList<FeedDataObject> feedData) {
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
        TextView title= (TextView) rowView.findViewById(R.id.list_title);
        ImageView download= rowView.findViewById(R.id.downloadImage);
        ImageView logo= rowView.findViewById(R.id.logo);
        final ImageView share= rowView.findViewById(R.id.shareImage);
        ImageView delete= rowView.findViewById(R.id.delete);
        final RelativeLayout shade= rowView.findViewById(R.id.shade);

        String titleText=feedData.get(position).getSource();
        title.setText(titleText);
        if (titleText.equalsIgnoreCase("facebook")) {
            logo.setImageResource(R.drawable.facebook);
        } else if (titleText.equalsIgnoreCase("instagram")) {
            logo.setImageResource(R.drawable.instagram);
        } else {
            logo.setImageResource(R.drawable.youtube);
        }

        if(feedData.get(position).getIsVideo()) {
            videoView.setVideoSize(Integer.parseInt(feedData.get(position).getWidth()),Integer.parseInt(feedData.get(position).getHeight()));
            videoView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            videoView.setVideoPath(feedData.get(position).getUrl());
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
                    shade.setVisibility(View.VISIBLE);
                }
            });
            videoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(videoView.isPlaying()){
                        videoView.pause();
                        videoPlayButton.setVisibility(View.VISIBLE);
                        shade.setVisibility(View.VISIBLE);
                    }else {
                        videoView.start();
                        videoPlayButton.setVisibility(View.INVISIBLE);
                        shade.setVisibility(View.INVISIBLE);
                    }
                }
            });
            videoView.seekTo(1);
        }else{
            videoView.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            videoPlayButton.setVisibility(View.GONE);
            Picasso.get().load(feedData.get(position).getUrl()).into(imageView);
        }

        download.setOnClickListener(new ActionButtonListener(position));
        share.setOnClickListener(new ActionButtonListener(position));
        delete.setOnClickListener(new ActionButtonListener(position));
        return rowView;
    }

    public void download(int position, FeedAdapter feedAdapter){
        Downloader downloader =new Downloader();
        if(!feedData.get(position).getIsVideo()){
            downloader.downloadRequest(feedData.get(position).getUrl(),feedData.get(position).getName(),".jpg",context,feedAdapter,position);
        }else {
            downloader.downloadRequest(feedData.get(position).getUrl(),feedData.get(position).getName(),".mp4",context,feedAdapter,position);
        }
    }

    public void share(int position){
        String path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        File file;
        Intent share = new Intent(Intent.ACTION_SEND);
        if(!feedData.get(position).getIsVideo()){
            share.setType("image/jpg");
            file =new File(path,feedData.get(position).getName()+".jpg");
        }else {
            share.setType("video/mp4");
            file =new File(path,feedData.get(position).getName()+".mp4");
        }
        share.putExtra(Intent.EXTRA_STREAM,Uri.parse(file.toString()));
        context.startActivity(Intent.createChooser(share, "Share with"));
    }

    @Override
    public void onDownloadComplete(int position) {
        share(position);
    }
    private void delete(int position) {
        ((MainActivity)context).delete(feedData.get(position).getID());
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
                case R.id.delete:
                    AlertDialog.Builder errorDialog = new AlertDialog.Builder(context);
                    errorDialog.setMessage("Are you sure, you want to delete?");
                    errorDialog.setPositiveButton("delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            delete(position);
                        }
                    });
                    errorDialog.setNegativeButton("Cancel",null);
                    errorDialog.show();
                    break;
            }
        }
    }
}
