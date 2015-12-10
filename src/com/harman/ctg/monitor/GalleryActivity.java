package com.harman.ctg.monitor;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.ctg.ui.Base;
import com.ctg.ui.R;
import com.harman.ctg.monitor.adapters.GalleryViewAdapter;
import com.harman.ctg.monitor.models.FileModel;
import com.harman.ctg.monitor.models.VideoItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;

public class GalleryActivity extends Activity implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    @Bind(R.id.gv_video_grid)
    GridView gallery;

    private ArrayList<VideoItem> galleryItems = new ArrayList<VideoItem>();
    private GalleryViewAdapter galleryAdapter;
    private Realm realm;

    private ActionMode actionMenu = null;
    private VideoItem itemLongClicked = null;
    private int itemPositionLongClicked = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.monitor_gallery);
        ButterKnife.bind(this);

        realm = Realm.getInstance(getApplicationContext());

        galleryAdapter = new GalleryViewAdapter(getApplicationContext(), galleryItems);
        gallery.setAdapter(galleryAdapter);
        gallery.setOnItemClickListener(this);
        gallery.setOnItemLongClickListener(this);

        initializeGallary();
        
        
    }

    public void initializeGallary() {
        galleryItems.clear();

        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), getResources().getString(R.string.monitor_name));
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                Arrays.sort(files, new Comparator<File>() {
                    public int compare(File f1, File f2) {
                        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                    }
                });

                for (File file : files) {
                    String url = file.getAbsolutePath();

                    Date date = new Date();
                    date.setTime(file.lastModified());

                    boolean locked = false;
                    RealmResults<FileModel> r = realm.where(FileModel.class).equalTo("filename", file.getName()).findAll();
                    if (r.size() > 0) {
                        locked = r.first().getLocked();
                    }

                    MemoryCacheUtils.removeFromCache(Uri.fromFile(file).toString(), ImageLoader.getInstance().getMemoryCache());
                    galleryItems.add(new VideoItem(url, null, date, locked));
                }
            }
        }

        if(galleryItems.size() == 0)
        {
    		Toast.makeText(this, "行车记录为空", Toast.LENGTH_LONG).show();
        	finish();
        	return;
        }
        
        galleryAdapter.notifyDataSetChanged();
        

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        VideoItem item = (VideoItem) parent.getItemAtPosition(position);
        File file = new File(item.url);

        // replay with intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "video/*");
        startActivity(intent);
    }


    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        itemLongClicked = (VideoItem) parent.getItemAtPosition(position);
        itemPositionLongClicked = position;

        if(actionMenu == null) {
            actionMenu = startActionMode(actionMode);
        }

        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
        realm.close();
    }

    //////////////////////////////////////////////////////////////////
    private ActionMode.Callback actionMode = new ActionMode.Callback() {
        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("");
            mode.getMenuInflater().inflate(R.menu.menu_popup, menu);
            return true;
        }

        // Called each time the action mode is shown.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if(itemLongClicked != null) {
                if (itemLongClicked.locked) {
                    mode.getMenu().getItem(0).setIcon(R.drawable.unlock);  // 0 - do not change the xml file
                } else {
                    mode.getMenu().getItem(0).setIcon(R.drawable.lock);  // 0 - do not change the xml file
                }
                return true;
            }
            else
                return false;  // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_popup_unlock:
                    unlock();
                    mode.finish(); // Action picked, so close the contextual menu
                    return true;
                case R.id.menu_popup_delete:
                    delete();
                    mode.finish(); // Action picked, so close the contextual menu
                case R.id.menu_popup_share:
                    share();
                    mode.finish(); // Action picked, so close the contextual menu
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMenu = null;
        }
    };

    //////////////////////////////////////////////////////////////////////////////
    private void unlock() {
        if(itemLongClicked != null) {
            File file = new File(itemLongClicked.url);

            // share or lock/unlock
            itemLongClicked.setLocked(!itemLongClicked.locked);
            galleryItems.set(itemPositionLongClicked, itemLongClicked);

            int start = gallery.getFirstVisiblePosition();
            View view = gallery.getChildAt(itemPositionLongClicked - start);
            gallery.getAdapter().getView(itemPositionLongClicked, view, gallery);

            realm.beginTransaction();
            RealmResults<FileModel> r = realm.where(FileModel.class).equalTo("filename", file.getName()).findAll();
            if (r.size() > 0) {
                r.first().setLocked(itemLongClicked.locked);
            }
            realm.commitTransaction();
        }

        itemLongClicked = null;
    }

    private void delete() {
        if(itemLongClicked != null) {
            File file = new File(itemLongClicked.url);
            boolean deleted = file.delete();
            if(deleted) {
                realm.beginTransaction();
                RealmResults<FileModel> r = realm.where(FileModel.class).equalTo("filename", file.getName()).findAll();
                if (r.size() > 0) {
                    r.first().removeFromRealm();
                }
                realm.commitTransaction();

                MemoryCacheUtils.removeFromCache(Uri.fromFile(new File(itemLongClicked.url)).toString(), ImageLoader.getInstance().getMemoryCache());

                galleryItems.remove(itemPositionLongClicked);
                galleryAdapter.notifyDataSetChanged();
            }
        }

        itemLongClicked = null;
    }

    private void share() {
        if(itemLongClicked != null) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);

            Uri uri = Uri.fromFile(new File(itemLongClicked.getUrl()));
            shareIntent.setType("video/mp4");

            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share to ..."));
        }

        itemLongClicked = null;
    }
}
