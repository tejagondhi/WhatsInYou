package me.tejagondhi.whatsinyou;


import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;

import me.tejagondhi.whatsinyou.CallBacks.InstagramObjectReady;
import me.tejagondhi.whatsinyou.Data.DBHelper;
import me.tejagondhi.whatsinyou.Data.Instagram;
import me.tejagondhi.whatsinyou.Network.InstagramDownloader;

public class MainActivity extends AppCompatActivity implements InstagramObjectReady {

    DBHelper dbHelper;
    private ListView feedListView;
    RecyclerView storyView;
    CharSequence clip;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        copyText();
    }

    private void copyText() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if(clipboard !=null && clipboard.hasPrimaryClip()&&clipboard.getPrimaryClip()!=null){
            clip = clipboard.getPrimaryClip().getItemAt(0).coerceToText(MainActivity.this).toString();
            if(clip.toString().contains("facebook")){

            }
            new InstagramDownloader(clip.toString().split("\\?")[0]+"?__a=1",MainActivity.this);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        feedListView = findViewById(R.id.listFeed);
        storyView = findViewById(R.id.story_view);
        displayFeed();
        if (isStoragePermissionGranted()){
            displayStory();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                displayStory();
            } else {
                android.app.AlertDialog.Builder alertDialog =new AlertDialog.Builder(this);
                alertDialog.setMessage("You cannot use this app without storage permission, click ok to exit the app, To enable permission relaunch app again");
                alertDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                });
                alertDialog.setNegativeButton("Give permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", MainActivity.this.getPackageName(), null);
                        intent.setData(uri);
                        MainActivity.this.startActivity(intent);
                    }
                });
                alertDialog.show();
            }
        }
    }



    public void displayFeed(){
        dbHelper = new DBHelper(this);
        FeedAdapter feedAdapter=new FeedAdapter(this,dbHelper.getFeed());
        feedListView.setAdapter(feedAdapter);
    }

    public void displayStory(){
        File whatsAppStatusDir= Environment.getExternalStoragePublicDirectory("/WhatsApp/Media/.Statuses");
        File[] statusFiles = whatsAppStatusDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                ContentResolver contentResolver = getContentResolver();
                String type = contentResolver.getType(FileProvider.getUriForFile(MainActivity.this,getPackageName()+".provider",new File(dir.getAbsolutePath()+File.separator+name)));
                if (type != null) {
                    return type.startsWith("image") || type.startsWith("video");
                }
                return false;
            }
        });
        HashMap<File, Bitmap> thumbNails = new HashMap<>();
        if (statusFiles != null) {
            for (File file : statusFiles ) {
                ContentResolver contentResolver = getContentResolver();
                String type = contentResolver.getType(FileProvider.getUriForFile(this,getPackageName()+".provider",file));
                if (type != null) {
                    if (type.startsWith("video")) {
                        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                        thumbNails.put(file,bitmap);
                    }
                }
            }
        }
        StoryAdapter storyAdapter =new StoryAdapter(statusFiles,thumbNails);
        storyView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false));
        storyView.setAdapter(storyAdapter);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onInstagramObjectReady(Instagram instagram) {
        if(dbHelper.insertFeed(instagram.getName(),instagram.getUrl(),instagram.getOriginalURL(),instagram.getIsVideo()?"VIDEO":"IMAGE","INSTAGRAM",instagram.getHeight(),instagram.getWidth())){
            displayFeed();
        }
    }
}