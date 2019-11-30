package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements ListFragment.itemSelectedInterface,
        ViewPagerFragment.PageChangedInterface, DetailsFragment.BookPlayedInterface {
    ArrayList<Book> books;

    Fragment detailFragment;
    Fragment listFragment;
    Fragment viewPagerFragment;

    Book currentlyPlaying;
    public static final String CURRENTLY_PLAYING_KEY = "playing";
    SeekBar progressBar;
    TextView nowPlaying;

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
            Log.d(TAG, "Books obtained");
            try {
                while(books.size() > 0) {books.remove(0);}
                notifyFragments();
                JSONArray jsonArray = new JSONArray((String) msg.obj);
                int i = 0;
                JSONObject jsonObject;
                while((jsonObject = jsonArray.getJSONObject(i++)) != null) {
                    books.add(new Book(jsonObject));
                }
            }catch (JSONException e){}
            Log.d(TAG, "Obtained " + books.size() + " books");
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
            Log.d(TAG, "Obtained book number " + currentBook);
        }
        else {
            currentBook = 0;
            currentlyPlaying = null;
        }

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

        findViewById(R.id.pauseButton).setOnClickListener(v ->{
            if(playerBound){
                player.pause();
            }
        });

        findViewById(R.id.stopButton).setOnClickListener(v ->{
            if(playerBound){
                player.stop();
                progressBar.setProgress(0);
                nowPlaying.setText("");
                currentlyPlaying = null;
            }
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
        unbindService(connection);
        playerBound = false;
    }

    private void getBooks(){
        Log.d(TAG, "getting books");
        if(singlePane){
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.ListContainer);
            if(fragment instanceof ListFragment){
                books = ((ListFragment)fragment).getBooks();
            }else{
                queryForBooks(this.getResources().getString(R.string.url));
            }
        }else{
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.DetailsContainer);
            if(fragment instanceof ViewPagerFragment){
                books = ((ViewPagerFragment)fragment).getBooks();
            }else{
                queryForBooks(this.getResources().getString(R.string.url));
            }
        }
    }

    private void queryForBooks(final String search){
        Log.d(TAG, "Querying for books");
        Log.d(TAG, search);
        new Thread(){
            public void run(){
                Log.d(TAG, "Thread started");
                try {
                    URL url = new URL(search);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(url.openStream()));
                    Log.d(TAG, "Opened Stream");
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

    public void notifyFragments(){
        if(viewPagerFragment instanceof ViewPagerFragment)
            ((ViewPagerFragment)viewPagerFragment).updateAdapter();
        else if(listFragment instanceof ListFragment) {
            ((ListFragment) listFragment).updateAdapter();
            if(books.size() > 0)
                ((DetailsFragment)detailFragment).DisplayBook(books.get(0));
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
            player.play(book.getId());
            currentlyPlaying = book;
            progressBar.setMax(book.getDuration());
            nowPlaying.setText(getString(R.string.nowPlaying) + currentlyPlaying.getTitle());
        }
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
