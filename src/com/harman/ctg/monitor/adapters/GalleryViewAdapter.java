package com.harman.ctg.monitor.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ctg.ui.R;
import com.harman.ctg.monitor.models.VideoItem;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by XiaXu on 2015-7-20.
 */
public class GalleryViewAdapter extends ArrayAdapter<VideoItem> {
    private DisplayImageOptions options;

    public GalleryViewAdapter(Context context, ArrayList<VideoItem> items) {
        super(context, R.layout.video_grid_item, items);
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.thumbnail_placeholder)
                .showImageForEmptyUri(R.drawable.thumbnail_placeholder)
                .showImageOnFail(R.drawable.thumbnail_placeholder)
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .considerExifParams(true)
                .build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .defaultDisplayImageOptions(options)
                .build();

        ImageLoader.getInstance().init(config);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if(view == null) {
            // inflate the GridView item layout
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.video_grid_item, parent, false);

            // initialize the view holder
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            // recycle the already inflated view
            holder = (ViewHolder) view.getTag();
        }

        // update the item view
        final VideoItem item = getItem(position);
        File f = new File(item.getUrl());
        ImageLoader.getInstance()
            .displayImage(Uri.fromFile(f).toString(), holder.thumbnail, options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    // no-op
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    holder.indicator.setVisibility(View.GONE);
                    holder.lock.setVisibility(View.GONE);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    holder.indicator.setVisibility(View.VISIBLE);
                    holder.lock.setVisibility(item.locked ? View.VISIBLE : View.GONE);
                    holder.date.setText(item.date.toString());
                }
            }, new ImageLoadingProgressListener() {
                @Override
                public void onProgressUpdate(String imageUri, View view, int current, int total) {
                    //no-op
                }
            });

        return view;
    }

    static class ViewHolder {
        @Bind(R.id.video_grid_thumbnail) ImageView thumbnail;
        @Bind(R.id.video_indicator) ImageView indicator;
        @Bind(R.id.video_date) TextView date;
        @Bind(R.id.video_lock) ImageView lock;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

}