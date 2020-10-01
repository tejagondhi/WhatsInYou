package me.tejagondhi.whatsinyou.Network;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import java.io.File;
import java.util.Objects;

import me.tejagondhi.whatsinyou.CallBacks.DownloadComplete;

public class Downloader {

    public void downloadRequest(String url, String newName, String ext, final Context context, final DownloadComplete callback, final int position){
        final String path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        final ProgressDialog progressDialog =new ProgressDialog(context);
        progressDialog.setMessage("Downloading");
        PRDownloader.download(url, path, newName+"."+ext)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {

                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {

                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {
                        progressDialog.show();
                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        progressDialog.dismiss();
                        if (callback!=null){
                            callback.onDownloadComplete(position);
                        }else{
                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            Uri contentUri = Uri.fromFile(new File(path));
                            mediaScanIntent.setData(contentUri);
                            Objects.requireNonNull(context).sendBroadcast(mediaScanIntent);
                            AlertDialog.Builder alertDialog =new AlertDialog.Builder(context);
                            alertDialog.setMessage("Download success");
                            alertDialog.setPositiveButton("ok", null);
                            alertDialog.show();
                        }
                    }

                    @Override
                    public void onError(Error error) {
                        progressDialog.dismiss();
                        if(error.getServerErrorMessage().equalsIgnoreCase("URL signature expired")){
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
                        }else{
                            Toast.makeText(context, error.getServerErrorMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }
}
