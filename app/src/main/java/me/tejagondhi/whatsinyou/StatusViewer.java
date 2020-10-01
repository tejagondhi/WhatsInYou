package me.tejagondhi.whatsinyou;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.tejagondhi.whatsinyou.CustomViews.FeedVideoView;


public class StatusViewer extends AppCompatActivity {
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        file= (File) getIntent().getExtras().get("file");
        setContentView(R.layout.activity_status_viewer);
        ImageView statusImageView = findViewById(R.id.statusImageView);
        FeedVideoView statusVideoView = findViewById(R.id.statusVideoView);
        ImageView statusDownload=findViewById(R.id.statusDownloadImage);
        ImageView statusShare=findViewById(R.id.statusShareImage);
        ContentResolver contentResolver = getContentResolver();
        String type = contentResolver.getType(FileProvider.getUriForFile(this,getPackageName()+".provider",file));
        if (type != null) {
            if (type.startsWith("image")){
                statusVideoView.setVisibility(View.GONE);
                statusImageView.setVisibility(View.VISIBLE);
                Picasso.get().load(file).into(statusImageView);
            }else if (type.startsWith("video")){
                MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
                mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
                statusVideoView.setVisibility(View.VISIBLE);
                statusImageView.setVisibility(View.GONE);
                statusVideoView.setVideoPath(file.getAbsolutePath());
                statusVideoView.setVideoSize(Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)),Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)));
                statusVideoView.start();
            }
        }
        statusDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download(false);
            }
        });

        statusShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download(true);
            }
        });
    }

    private void download( Boolean share) {
        String destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()+ File.separator +file.getName();
        File destination = new File(destinationPath);
        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileUtils.copy(new FileInputStream(file), new FileOutputStream(destination));
                copyCheck(destination,share);


            }else{
                copy(file,destination);
                copyCheck(destination, share);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void share(File file){
        Intent share = new Intent(Intent.ACTION_SEND);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        ContentResolver contentResolver = getContentResolver();
        String type = contentResolver.getType(FileProvider.getUriForFile(this,getPackageName()+".provider",file));
        if (type != null) {
            if (type.startsWith("image")){
                share.setType("image/jpg");
            }else if (type.startsWith("video")){
                share.setType("video/mp4");
            }
        }
        share.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file));
        startActivity(Intent.createChooser(share, "Share with"));
    }

    public void copyCheck(File destination, Boolean share){
        if (destination.exists()){
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(destination);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
            if (share){
                share(destination);
            }else {
                AlertDialog.Builder alertDialog =new AlertDialog.Builder(this);
                alertDialog.setMessage("Download success");
                alertDialog.setPositiveButton("ok", null);
                alertDialog.show();
            }
        }
    }

    public static void copy(File src, File dst) throws IOException {
        try (InputStream in = new FileInputStream(src)) {
            try (OutputStream out = new FileOutputStream(dst)) {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        }
    }
}