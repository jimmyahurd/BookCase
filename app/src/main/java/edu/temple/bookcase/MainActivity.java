package edu.temple.bookcase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ListFragment.itemSelectedInterface, ViewPagerFragment.PageChangedInterface {
    ArrayList<String> books;

    Fragment detailFragment;
    Fragment listFragment;
    Fragment viewPagerFragment;

    int currentBook;
    public static final String CURRENT_BOOK_KEY = "current";

    boolean singlePane;

    final String TAG = "MainActivity";

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

        books = new ArrayList<String>(Arrays.asList(this.getResources().getStringArray(R.array.books)));

        if(findViewById(R.id.ListContainer) == null)
            singlePane = true;
        else
            singlePane = false;

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
                detailFragment = DetailsFragment.newInstance(books.get(currentBook));
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
