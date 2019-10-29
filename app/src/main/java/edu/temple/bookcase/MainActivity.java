package edu.temple.bookcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ListFragment.itemSelectedInterface {
    ArrayList<String> books;

    Fragment detailFragment;
    Fragment listFragment;
    Fragment viewPagerFragment;

    boolean singlePane;

    final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "test");

        books = new ArrayList<String>(Arrays.asList(this.getResources().getStringArray(R.array.books)));

        if(findViewById(R.id.ListContainer) == null)
            singlePane = true;
        else
            singlePane = false;

        if(singlePane){
            viewPagerFragment = getSupportFragmentManager().findFragmentById(R.id.DetailsContainer);
            if(!(viewPagerFragment instanceof ViewPagerFragment)){
                viewPagerFragment = ViewPagerFragment.newInstance(books);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.DetailsContainer, viewPagerFragment)
                        .commit();
            }
        }else{
            detailFragment = getSupportFragmentManager().findFragmentById(R.id.DetailsContainer);
            if(!(detailFragment instanceof DetailsFragment)){
                detailFragment = DetailsFragment.newInstance(books.get(0));
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
        Log.d(TAG, books.get(position) + "was clicked");
        ((DetailsFragment)detailFragment).DisplayBook(books.get(position));
    }
}
