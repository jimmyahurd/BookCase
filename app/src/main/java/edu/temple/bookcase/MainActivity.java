package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements ListFragment.itemSelectedInterface,
        ViewPagerFragment.PageChangedInterface, DetailsFragment.BookInterface {
    ArrayList<Book> books;

    Fragment detailFragment;
    Fragment listFragment;
    Fragment viewPagerFragment;

    Book currentlyPlaying;
    public static final String CURRENTLY_PLAYING_KEY = "playing";
    SeekBar progressBar;
    TextView nowPlaying;

    ImageButton playPause;
    boolean playing;

    EditText query;

    int currentBook;
    public static final String CURRENT_BOOK_KEY = "current";

    boolean singlePane;

    AudiobookService.MediaControlBinder player;
    boolean playerBound = false;

    final String TAG = "MainActivity";

    Handler queryHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            //Log.d(TAG, "Books obtained");
            try {
                while(books.size() > 0) {books.remove(0);}
                notifyFragments();
                JSONArray jsonArray = new JSONArray((String) msg.obj);
                for(int i = 0; i < jsonArray.length(); i++){
                    books.add(new Book(jsonArray.getJSONObject(i), false));
                }
            }catch (JSONException e){}
            //Log.d(TAG, "Obtained " + books.size() + " books");
            checkDownloads();
            notifyFragments();
            return false;
        }
    });

    Handler bookProgressHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            AudiobookService.BookProgress progress = (AudiobookService.BookProgress) msg.obj;
            if(progress != null)
                progressBar.setProgress(progress.getProgress());
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null) {
            currentBook = savedInstanceState.getInt(CURRENT_BOOK_KEY);
            currentlyPlaying = savedInstanceState.getParcelable(CURRENTLY_PLAYING_KEY);
            //Log.d(TAG, "Obtained book number " + currentBook);
        }
        else {
            currentBook = 0;
            currentlyPlaying = null;
        }
        playing = true;

        if(findViewById(R.id.ListContainer) == null)
            singlePane = true;
        else
            singlePane = false;

        books = new ArrayList<>();
        getBooks();

        if(singlePane){
            viewPagerFragment = getSupportFragmentManager().findFragmentById(R.id.DetailsContainer);
            if(!(viewPagerFragment instanceof ViewPagerFragment)){
                viewPagerFragment = ViewPagerFragment.newInstance(books, currentBook);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.DetailsContainer, viewPagerFragment)
                        .commit();
            }
        }else{
            detailFragment = getSupportFragmentManager().findFragmentById(R.id.DetailsContainer);
            if(!(detailFragment instanceof DetailsFragment)){
                if(books.size() > 0)
                    detailFragment = DetailsFragment.newInstance(books.get(currentBook));
                else
                    detailFragment = new DetailsFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.DetailsContainer, detailFragment)
                        .commit();
            }
            listFragment = getSupportFragmentManager().findFragmentById(R.id.ListContainer);
            if(!(listFragment instanceof ListFragment)) {
                listFragment = ListFragment.newInstance(books);
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.ListContainer, listFragment)
                        .commit();
            }
        }

        query = findViewById(R.id.query);

        findViewById(R.id.searchButton).setOnClickListener(v ->{
            queryForBooks(getResources().getString(R.string.url) + "?search=" + query.getText());
        });

        playPause = findViewById(R.id.playPauseButton);

        playPause.setOnClickListener(v ->{
            if(playerBound && currentlyPlaying != null){
                player.pause();
                currentlyPlaying.setProgress(progressBar.getProgress());
                if(playing) {
                    playPause.setImageResource(R.mipmap.play);
                    playing = false;
                }else{
                    playPause.setImageResource(R.mipmap.pause);
                    playing = true;
                }
            }
        });

        findViewById(R.id.stopButton).setOnClickListener(v ->{
            currentlyPlaying.setProgress(0);
            stopAudio();
        });

        progressBar = findViewById(R.id.progressBar);
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                         if(playerBound && player.isPlaying()){
                        player.seekTo(progress);
                    }
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        nowPlaying = findViewById(R.id.nowPlaying);

        if(currentlyPlaying != null) {
            progressBar.setMax(currentlyPlaying.getDuration());
            nowPlaying.setText(getString(R.string.nowPlaying) + currentlyPlaying.getTitle());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AudiobookService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        try {
            Log.e(TAG, "Writing to file");
            File file = new File(getFilesDir(), getString(R.string.booksFile));
            if(!file.exists())
                file.createNewFile();
            String path = file.getPath();
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));

            JSONArray jsonArray = new JSONArray();
            if(currentlyPlaying == null)
                jsonArray.put(false);
            else{
                jsonArray.put(true);
                jsonArray.put(currentlyPlaying.toJSON());
            }
            for(int i = 0; i < books.size(); i++) {
                jsonArray.put(books.get(i).toJSON());
            }
            writer.write(jsonArray.toString());
            //Log.e("wrote", jsonArray.toString(6));

            //writer.write("test input");
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        unbindService(connection);
        playerBound = false;
    }

    private void getBooks(){
        File file = new File(getFilesDir(), getString(R.string.booksFile));

        if(file.length() == 0)
            Log.e(TAG, "file is empty");
        else {
            try {
                if (file.exists()) {
                    String path = file.getPath();
                    Log.e("path read", path);
                    BufferedReader reader = new BufferedReader(new FileReader(path));
                    String response;
                    StringBuilder builder = new StringBuilder();
                    while ((response = reader.readLine()) != null) {
                        builder.append(response);
                    }
                    JSONArray jsonArray = new JSONArray(builder.toString());
                    int i = 1;
                    if((boolean)jsonArray.get(0)) {
                        currentlyPlaying = new Book(jsonArray.getJSONObject(i), true);
                        i++;
                    }
                    for (; i < jsonArray.length(); i++) {
                        books.add(new Book(jsonArray.getJSONObject(i), true));
                        if(books.get(books.size() - 1).isDownloaded())
                            books.get(books.size() - 1).setAudio(new File(getExternalFilesDir(DOWNLOAD_SERVICE), books.get(books.size() - 1).getTitle()));
                    }
                    Log.d(TAG, "Grabbed books from file");

                    //String test = reader.readLine();
                    //Log.e(TAG, test==null? "Nothing":test);
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
                file.delete();
                Log.e(TAG, "file deleted");
            }
        }

        if(books.size() < 1){
            queryForBooks(this.getResources().getString(R.string.url));
        }
    }

    private void queryForBooks(final String search){
        //Log.d(TAG, "Querying for books");
        //Log.d(TAG, search);
        new Thread(){
            public void run(){
                //Log.d(TAG, "Thread started");
                try {
                    URL url = new URL(search);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(url.openStream()));
                    //Log.d(TAG, "Opened Stream");
                    String response;
                    StringBuilder builder = new StringBuilder();
                    while ((response = reader.readLine()) != null) {
                        builder.append(response);
                    }
                    Message msg = Message.obtain();
                    msg.obj = builder.toString();
                    queryHandler.sendMessage(msg);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void notifyFragments(){
        if(viewPagerFragment instanceof ViewPagerFragment)
            ((ViewPagerFragment)viewPagerFragment).updateAdapter();
        else if(listFragment instanceof ListFragment) {
            ((ListFragment) listFragment).updateAdapter();
            if(books.size() > 0)
                ((DetailsFragment)detailFragment).DisplayBook(books.get(0));
        }
    }

    private void checkDownloads(){
        File audio;
        for(int i = 0; i < books.size(); i++){
            audio = new File(getExternalFilesDir(DOWNLOAD_SERVICE), books.get(i).getTitle());
            if(audio.exists()){
                books.get(i).setAudio(audio);
            }
        }
    }

    @Override
    public void itemSelected(int position) {
        Log.d(TAG, books.get(position) + " was clicked");
        ((DetailsFragment)detailFragment).DisplayBook(books.get(position));
        currentBook = position;
    }

    public void pageChanged(int position){
        currentBook = position;
    }

    @Override
    public void playPressed(Book book) {
        if(!player.isPlaying()) {
            startService(new Intent(this, AudiobookService.class));
        }else{
            currentlyPlaying.setProgress(progressBar.getProgress());
            stopAudio();
        }
        playBook(book);
    }

    private void playBook(Book book){
        if(book.isDownloaded()){
            player.play(book.getAudio(), book.getProgress()>10? book.getProgress() - 10 : 0);
        }else {
            player.play(book.getId());
        }
        currentlyPlaying = book;
        progressBar.setMax(book.getDuration());
        nowPlaying.setText(getString(R.string.nowPlaying) + currentlyPlaying.getTitle());
    }

    private void stopAudio(){
        if(playerBound){
            player.stop();
            progressBar.setProgress(0);
            nowPlaying.setText("");
            currentlyPlaying = null;
            stopService(new Intent(this, AudiobookService.class));
        }
    }

    @Override
    public void downloadOrDelete(Book book) {
        if(book.isDownloaded()){
            File file = new File(getExternalFilesDir(DOWNLOAD_SERVICE), book.getTitle());
            file.delete();
            book.deleteAudio();
        }else{
            downloadBook(book);
            book.setAudio(new File(getExternalFilesDir(DOWNLOAD_SERVICE), book.getTitle()));
        }
    }

    private void downloadBook(Book book){
        new Thread(){
            public void run(){
                try {
                    Log.e(TAG, "Download Started");
                    URL url = new URL(getString(R.string.audioURL) + book.getId());
                    InputStream reader = url.openStream();

                    File file = new File(getExternalFilesDir(DOWNLOAD_SERVICE), book.getTitle());
                    file.createNewFile();
                    FileOutputStream writer = new FileOutputStream(file.getPath());

                    byte[] buffer = new byte[4096];

                    while (reader.read(buffer) != -1) {
                        writer.write(buffer);
                    }

                    reader.close();
                    writer.close();

                    Log.e(TAG, "Download finished");
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(CURRENT_BOOK_KEY, currentBook);
        if(currentlyPlaying != null)
            outState.putParcelable(CURRENTLY_PLAYING_KEY, currentlyPlaying);
        else
            outState.putParcelable(CURRENTLY_PLAYING_KEY, null);
        Log.d(TAG, "Saved book number " + currentBook);
        super.onSaveInstanceState(outState);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            player = (AudiobookService.MediaControlBinder) service;
            player.setProgressHandler(bookProgressHandler);
            playerBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playerBound = false;
        }
    };
}
