package me.tejagondhi.whatsinyou;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {

    private final File[] statusFiles;
    private final HashMap<File, Bitmap> thumbNails;


    public StoryAdapter(File[] statusFiles, HashMap<File, Bitmap> thumbNails) {
        this.statusFiles=statusFiles;
        this.thumbNails=thumbNails;
    }


    @NonNull
    @Override
    public StoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater= LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.status_listview,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final StoryAdapter.ViewHolder holder, final int position) {
        ContentResolver contentResolver = holder.storyImageView.getContext().getContentResolver();
        String type = contentResolver.getType(FileProvider.getUriForFile(holder.storyImageView.getContext(),holder.storyImageView.getContext().getPackageName()+".provider",statusFiles[position]));
        if (type != null) {
            if (type.startsWith("image")){
                Picasso.get().load(statusFiles[position]).into(holder.storyImageView);
            }else if (type.startsWith("video")){
                holder.storyImageView.setImageBitmap(thumbNails.get(statusFiles[position]));
            }
        }
        holder.storyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.storyImageView.getContext(),StatusViewer.class);
                intent.putExtra("file",statusFiles[position]);
                holder.storyImageView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return statusFiles.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView storyImageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            storyImageView = (CircleImageView) itemView.findViewById(R.id.status_listView);

        }
    }
}
