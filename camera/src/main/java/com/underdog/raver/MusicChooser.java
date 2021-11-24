package com.underdog.raver;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

public class MusicChooser extends AppCompatActivity {
    private ListView audiolistView; // 리스트뷰
    private ArrayList<Song_Item> songsList = null; // 데이터 리스트
    private ListViewAdapter listViewAdapter = null; // 리스트뷰에 사용되는 ListViewAdapter

    Context context;

    @Override
    protected void onStart() {
        super.onStart();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("\n음악 선택 후 동영상 촬영이 가능합니다.")
                .setTitle("녹화시 사용할 음악 선택")
                .setCancelable(true)
                .setPositiveButton("확인", (dialog, id) -> {
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_layout);
        getSupportActionBar().setTitle("음악 선택");
        getSupportActionBar().hide();

        context = this.getBaseContext();

        // Adapter에 추가 데이터를 저장하기 위한 ArrayList
        songsList = new ArrayList<Song_Item>(); // ArrayList 생성
        audiolistView = (ListView) findViewById(R.id.my_listView);
        listViewAdapter = new ListViewAdapter(this); // Adapter 생성
        audiolistView.setAdapter(listViewAdapter); // 어댑터를 리스트뷰에 세팅
        audiolistView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        audiolistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                startActivity(new Intent(MusicChooser.this, PortraitCameraActivity.class).putExtra("music_path",songsList.get(i).DataPath));
                overridePendingTransition(0, 0);

            }
        });
        loadAudio();

    }

    class ViewHolder {
        public ImageView mImgAlbumArt;
        public TextView mTitle;
        public TextView mSubTitle;
        public TextView mDuration;

    }

    private class ListViewAdapter extends BaseAdapter {
        Context context;

        public ListViewAdapter(Context context) {
            this.context = context;
        }

        // 음악 데이터 추가를 위한 메소드
        public void addItem(long mId, long AlbumId, String Title, String Artist, String Album, long Duration, String DataPath, boolean checkItem_flag) {
            Song_Item item = new Song_Item();
            item.setmId(mId);
            item.setAlbumId(AlbumId);
            item.setTitle(Title);
            item.setArtist(Artist);
            item.setAlbum(Album);
            item.setDuration(Duration);
            item.setDataPath(DataPath);
            songsList.add(item);
        }


        @Override
        public int getCount() {
            return songsList.size(); // 데이터 개수 리턴
        }

        @Override
        public Object getItem(int position) {
            return songsList.get(position);
        }

        // 지정한 위치(position)에 있는 데이터 리턴
        @Override
        public long getItemId(int position) {
            return position;
        }

        // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            final Context context = parent.getContext();
            final Integer index = Integer.valueOf(position);

            // 화면에 표시될 View
            if (convertView == null) {
                viewHolder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.song_item, parent, false);

                convertView.setBackgroundColor(0x00FFFFFF);
                convertView.invalidate();

                // 화면에 표시될 View 로부터 위젯에 대한 참조 획득
                viewHolder.mImgAlbumArt = (ImageView) convertView.findViewById(R.id.album_Image);
                viewHolder.mTitle = (TextView) convertView.findViewById(R.id.txt_title);
                viewHolder.mSubTitle = (TextView) convertView.findViewById(R.id.txt_subtitle);
                viewHolder.mDuration = (TextView) convertView.findViewById(R.id.txt_duration);


                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // PersonData 에서 position 에 위치한 데이터 참조 획득
            final Song_Item songItem = songsList.get(position);

            // 아이템 내 각 위젯에 데이터 반영
            /*Bitmap albumArt = MusicChooser.getArtworkQuick(context, (int)songItem.getAlbumId(), 100, 100);
            if(albumArt != null){
                viewHolder.mImgAlbumArt.setImageBitmap(albumArt);
            }*/

            viewHolder.mTitle.setText(songItem.getTitle());
            viewHolder.mSubTitle.setText(songItem.getArtist());

            int dur = (int) songItem.getDuration();
            int hrs = (dur / 3600000);
            int mns = (dur / 60000) % 60000;
            int scs = dur % 60000 / 1000;
            String songTime = String.format("%02d:%02d:%02d", hrs, mns, scs);
            if (hrs == 0) {
                songTime = String.format("%02d:%02d", mns, scs);
            }
            viewHolder.mDuration.setText(songTime);


            return convertView;
        }
    }


    // Album ID로 부터 Bitmap 이미지를 생성해 리턴해 주는 메소드
    private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    // Get album art for specified album. This method will not try to
    // fall back to getting artwork directly from the file, nor will it attempt to repair the database.
    private static Bitmap getArtworkQuick(Context context, int album_id, int w, int h) {
        w -= 2;
        h -= 2;
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            ParcelFileDescriptor fd = null;
            try {
                fd = res.openFileDescriptor(uri, "r");
                int sampleSize = 1;

                sBitmapOptionsCache.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while (nextWidth>w && nextHeight>h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }

                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(
                        fd.getFileDescriptor(), null, sBitmapOptionsCache);

                if (b != null) {
                    // finally rescale to exactly the size we need
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        b.recycle();
                        b = tmp;
                    }
                }

                return b;
            } catch (FileNotFoundException e) {
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    public class Song_Item {
        private long mId; // 오디오 고유 ID
        private long AlbumId; // 오디오 앨범아트 ID
        private String Title; // 타이틀 정보
        private String Artist; // 아티스트 정보
        private String Album; // 앨범 정보
        private long Duration; // 재생시간
        private String DataPath; // 실제 데이터 위치


        public Song_Item() {
        }

        public Song_Item(long mId, long albumId, String title, String artist, String album, long duration, String dataPath) {
            this.mId = mId;
            AlbumId = albumId;
            Title = title;
            Artist = artist;
            Album = album;
            Duration = duration;
            DataPath = dataPath;

        }

        public long getmId() {
            return mId;
        }

        public void setmId(long mId) {
            this.mId = mId;
        }

        public long getAlbumId() {
            return AlbumId;
        }

        public void setAlbumId(long albumId) {
            AlbumId = albumId;
        }

        public String getTitle() {
            return Title;
        }

        public void setTitle(String title) {
            Title = title;
        }

        public String getArtist() {
            return Artist;
        }

        public void setArtist(String artist) {
            Artist = artist;
        }

        public String getAlbum() {
            return Album;
        }

        public void setAlbum(String album) {
            Album = album;
        }

        public long getDuration() {
            return Duration;
        }

        public void setDuration(long duration) {
            Duration = duration;
        }

        public String getDataPath() {
            return DataPath;
        }

        public void setDataPath(String dataPath) {
            DataPath = dataPath;
        }


    }
    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);
        cursor.moveToFirst();
        System.out.println("음악파일 개수 = " + cursor.getCount());
        if (cursor != null && cursor.getCount() > 0) {
            do {
                long track_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                long albumId = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                long mDuration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                String datapath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                System.out.println("mId = " + track_id + " albumId = " +albumId+ " title : "+title+" album : "+album+" artist: "+artist+" 총시간 : "+mDuration+" data : "+datapath);
                // Save to audioList
                listViewAdapter.addItem(track_id,albumId,title,artist,album,mDuration,datapath,false);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }






}

