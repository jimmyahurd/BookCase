package edu.temple.bookcase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements ListFragment.itemSelectedInterface {
    ArrayList<String> books;
    Fragment detailFragment;

    final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e(TAG, "test");

        books = new ArrayList<String>(Arrays.asList(this.getResources().getStringArray(R.array.books)));

        boolean singlePane;
        if(findViewById(R.id.ViewPagerContainer) != null)
            singlePane = true;
        else
            singlePane = false;

        if(singlePane){
            
        }else{
            if(getSupportFragmentManager().findFragmentById(R.id.DetailsContainer) != null){
                detailFragment = getSupportFragmentManager().findFragmentById(R.id.DetailsContainer);
            }else {
                detailFragment = new DetailsFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.ListContainer, ListFragment.newInstance(books))
                        .add(R.id.DetailsContainer, detailFragment)
                        .commit();
            }
        }
    }

    @Override
    public void itemSelected(int position) {
        Log.d(TAG, books.get(position) + "was clicked");
        ((DetailsFragment)detailFragment).changeItem(books.get(position));
    }
}
