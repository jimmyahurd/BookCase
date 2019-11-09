package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

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

public class MainActivity extends AppCompatActivity implements ListFragment.itemSelectedInterface, ViewPagerFragment.PageChangedInterface {
    ArrayList<Book> books;

    Fragment detailFragment;
    Fragment listFragment;
    Fragment viewPagerFragment;

    int currentBook;
    public static final String CURRENT_BOOK_KEY = "current";

    boolean singlePane;

    final String TAG = "MainActivity";

    Handler queryHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            Log.d(TAG, "Books obtained");
            try {
                JSONArray jsonArray = new JSONArray((String) msg.obj);
                int i = 0;
                JSONObject jsonObject;
                while((jsonObject = jsonArray.getJSONObject(i++)) != null) {
                    books.add(new Book(jsonObject));
                }
            }catch (JSONException e){}
            Log.d(TAG, "Obtained " + books.size() + " books");
            if(viewPagerFragment instanceof ViewPagerFragment)
                ((ViewPagerFragment)viewPagerFragment).updateAdapter();
            else if(listFragment instanceof ListFragment)
                ((ListFragment)listFragment).updateAdapter();
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null) {
            currentBook = savedInstanceState.getInt(CURRENT_BOOK_KEY);
            Log.d(TAG, "Obtained book number " + currentBook);
        }
        else {
            currentBook = 0;
        }

        if(findViewById(R.id.ListContainer) == null)
            singlePane = true;
        else
            singlePane = false;

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
        books = new ArrayList<>();
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(CURRENT_BOOK_KEY, currentBook);
        Log.d(TAG, "Saved book number " + currentBook);
        super.onSaveInstanceState(outState);
    }
}
