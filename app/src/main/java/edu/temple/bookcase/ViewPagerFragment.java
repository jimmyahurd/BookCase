package edu.temple.bookcase;


import android.content.Context;
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


public class ViewPagerFragment extends Fragment {
    public static final String BOOKS_KEY = "books";
    ArrayList<String> books;

    public static final String CURRENT_KEY = "current";
    int current;

    ViewPager viewPager;

    PageChangedInterface parent;

    public ViewPagerFragment() {
        // Required empty public constructor
    }

    public static ViewPagerFragment newInstance(ArrayList<String> books, int current){
        ViewPagerFragment fragment = new ViewPagerFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(BOOKS_KEY, books);
        args.putInt(CURRENT_KEY, current);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ListFragment.itemSelectedInterface) {
            parent = (ViewPagerFragment.PageChangedInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PageChangedInterface");
        }
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            books = getArguments().getStringArrayList(BOOKS_KEY);
            current = getArguments().getInt(CURRENT_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewPager = (ViewPager) inflater.inflate(R.layout.fragment_view_pager, container, false);
        viewPager.setAdapter(new DetailFragmentPagerAdapter(
                getChildFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                books));
        viewPager.setCurrentItem(current);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override
            public void onPageScrollStateChanged(int state) {}
            @Override
            public void onPageSelected(int position) {
                parent.pageChanged(position);
            }
        });
        return viewPager;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parent = null;
    }

    private class DetailFragmentPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<DetailsFragment> fragments;

        public DetailFragmentPagerAdapter(@NonNull FragmentManager fm, int behavior, ArrayList<String> books) {
            super(fm, behavior);
            createFragments(books);
        }

        private void createFragments(ArrayList<String> books){
            fragments = new ArrayList<DetailsFragment>();
            for(int i = 0; i < books.size(); i++){
                fragments.add(DetailsFragment.newInstance(books.get(i)));
            }
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return books.size();
        }
    }

    public interface PageChangedInterface{
        void pageChanged(int position);
    }

}
