package edu.temple.bookcase;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


//use fragmentStatePagerAdapter
public class ViewPagerFragment extends Fragment {
    public static final String BOOKS_KEY = "books";
    ArrayList<String> books;

    ViewPager viewPager;

    public ViewPagerFragment() {
        // Required empty public constructor
    }

    public static ViewPagerFragment newInstance(ArrayList<String> books){
        ViewPagerFragment fragment = new ViewPagerFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(BOOKS_KEY, books);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            books = getArguments().getStringArrayList(BOOKS_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewPager = (ViewPager) inflater.inflate(R.layout.fragment_view_pager, container, false);
        viewPager.setAdapter(new DetailFragmentPagerAdapter(getChildFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT));
        return viewPager;
    }

    private class DetailFragmentPagerAdapter extends FragmentStatePagerAdapter {

        public DetailFragmentPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @Override
        public Fragment getItem(int position) {
            return DetailsFragment.newInstance(books.get(position));
        }

        @Override
        public int getCount() {
            return books.size();
        }
    }

}
