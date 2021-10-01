package com.underdog.raver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.underdog.raver.R;

import java.io.File;
import java.util.ArrayList;

public class MusicChooser extends AppCompatActivity {

    private ListView listView;
    private String songNames[];

    @Override
    protected void onStart() {
        super.onStart();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You Can Edit Music Later Next OR Record without Music..")
                .setTitle("Here is you Music List")
                .setCancelable(true)
                .setPositiveButton("OK", (dialog, id) -> {
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_chooser);
        getSupportActionBar().setTitle("Choose Background Music");

        listView = findViewById(R.id.listView);

        ArrayList<File> songs = readSongs(Environment.getExternalStorageDirectory());

        songNames = new String[songs.size()];

        for(int i=0;i<songs.size();++i){
            songNames[i] = songs.get(i).getName().toString().replace(".mp3","");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),R.layout.song_layout,R.id.textView,songNames);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(MusicChooser.this, PortraitCameraActivity.class).putExtra("position",i).putExtra("list",songs));
               /* Intent returnIntent = new Intent();
                returnIntent.putExtra("position",i);
                returnIntent.putExtra("list",songs);
                setResult(RESULT_OK,returnIntent);
                finish();*/
            }
        });
    }

    private ArrayList<File> readSongs(File root){
        ArrayList<File> arrayList = new ArrayList<File>();
        File files[] = root.listFiles();

        for(File file : files){
            if(file.isDirectory()){
                arrayList.addAll(readSongs(file));
            }else {
                if(file.getName().endsWith(".mp3")){
                    arrayList.add(file);
                }
            }
        }
        return arrayList;
    }
}