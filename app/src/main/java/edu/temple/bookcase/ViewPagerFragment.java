package edu.temple.bookcase;


import android.content.Context;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class ViewPagerFragment extends Fragment {
    public static final String BOOKS_KEY = "books";
    ArrayList<Book> books;

    public static final String CURRENT_KEY = "current";
    int current;

    ViewPager viewPager;
    DetailFragmentPagerAdapter adapter;

    PageChangedInterface parent;

    public ViewPagerFragment() {
        // Required empty public constructor
    }

    public static ViewPagerFragment newInstance(ArrayList<Book> books, int current){
        ViewPagerFragment fragment = new ViewPagerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(BOOKS_KEY, books);
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
            Log.d("ViewPager", "Grabbing arguments");
            books = getArguments().getParcelableArrayList(BOOKS_KEY);
            current = getArguments().getInt(CURRENT_KEY);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewPager = (ViewPager) inflater.inflate(R.layout.fragment_view_pager, container, false);
        adapter = new DetailFragmentPagerAdapter(
                getChildFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,
                books);
        viewPager.setAdapter(adapter);
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

    public ArrayList<Book> getBooks(){
        return books;
    }

    private class DetailFragmentPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<Book> books;

        public DetailFragmentPagerAdapter(@NonNull FragmentManager fm, int behavior, ArrayList<Book> books) {
            super(fm, behavior);
            this.books = books;
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

    public void updateAdapter(){
        adapter.notifyDataSetChanged();
    }

    public interface PageChangedInterface{
        void pageChanged(int position);
    }

}
