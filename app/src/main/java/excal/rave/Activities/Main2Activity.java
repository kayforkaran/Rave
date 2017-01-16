package excal.rave.Activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.jar.Manifest;

import excal.rave.Assistance.PlaySongs;
import excal.rave.R;

public class Main2Activity extends AppCompatActivity {
    //TODO: seekBar change
    ListView songsList;
    ArrayList<String> allSongTitles;
    ArrayList<String> allSongData;
    Cursor cursor;
    boolean[] itemState;
    int count;
    SparseArray<View> selectedViews;
    PlaySongs playSongs;
    ViewGroup layout;
    private boolean selectionMode = false;
    int currentPlaying = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        seekPermission();
        songsList = (ListView) findViewById(R.id.songs_list);
        selectedViews = new SparseArray<View>();
        layout = (ViewGroup) findViewById(R.id.activity_main2);
        playSongs = new PlaySongs(this, layout);
        playSongs.init();
        playSongs.setListeners();


    }

    @SuppressWarnings("deprecation")
    public void loadSongs() {
        int i = 0;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA};
        cursor = this.managedQuery(uri, projection, null, null, null);
        allSongTitles = new ArrayList<String>();
        allSongData = new ArrayList<String>();
        cursor.moveToFirst();
        int column_index = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        do {
            allSongTitles.add(cursor.getString(0));
            allSongData.add(cursor.getString(column_index));
        } while (cursor.moveToNext());
        //cursor.close();
        itemState = new boolean[allSongTitles.size()];
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadSongs();

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, allSongTitles) {
                        @NonNull
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            itemState[position] = false;
                            return super.getView(position, convertView, parent);
                        }
                    };
                    songsList.setAdapter(adapter);
                    //on long press on a view reverse the selection state of the view
                    //set selection code on the onItemClickListener active
                    //TODO: make a counter for the number of views selected
                    //use this counter's count to determine whether the code for selection is active or not


                    songsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (playSongs.mp.isPlaying()) {
                                playSongs.mp.stop();
                            }

                            if (itemState[i] && currentPlaying == i) {
                                selectedViews.append(i, view);
                                currentPlaying = -1;
                            } else if (itemState[i]) {
                                selectedViews.remove(i);
                                view.setBackgroundResource(R.color.deselectedItem);
                            } else {
                                view.setBackgroundResource(R.color.selectedItem);
                            }
                            selectionMode = true;
                            return false;
                        }
                    });

                    songsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (selectionMode) {
                                if (itemState[i] && currentPlaying == i) {
                                    selectedViews.append(i, view);
                                    currentPlaying = -1;
                                } else if (itemState[i]) {
                                    view.setBackgroundResource(R.color.deselectedItem);
                                    selectedViews.remove(i);
                                } else {
                                    view.setBackgroundResource(R.color.selectedItem);
                                    selectedViews.append(i, view);
                                }
                                itemState[i] = !itemState[i];
                            } else {
                                if (playSongs.mp.isPlaying() && currentPlaying == i) {
                                    playSongs.mp.pause();
                                    Toast.makeText(getApplicationContext(),"1st case",Toast.LENGTH_SHORT).show();
                                } else if ((!playSongs.mp.isPlaying()) && currentPlaying == i) {
                                   playSongs.mp.start();
                                    Toast.makeText(getApplicationContext(),"2nd case",Toast.LENGTH_SHORT).show();
                                } else if (currentPlaying != i) {
                                    currentPlaying = i;
                                    playSongs.playSong(i);
                                    Toast.makeText(getApplicationContext(),"3rd case",Toast.LENGTH_SHORT).show();
                                }
                            }
                            currentPlaying = i;
                        }
                    });

                } else {
                    Toast.makeText(this, "permission not granted", Toast.LENGTH_LONG).show();
                }

            }

        }
    }

    @Override
    public void onBackPressed() {
        if (selectedViews.size() != 0) {
            setSelectionModeOff();
        } else {
            super.onBackPressed();
        }
        selectionMode = false;
    }

    public void seekPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
    }

    public void setSelectionModeOff() {
        //layout change
        count = 0;
        for (int i = 0; i < selectedViews.size(); i++) {
            itemState[selectedViews.keyAt(i)] = false;
            selectedViews.valueAt(i).setBackgroundResource(R.color.deselectedItem);
        }
        selectedViews.clear();
    }
}
