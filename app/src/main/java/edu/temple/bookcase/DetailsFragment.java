package edu.temple.bookcase;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {
    private static final String ITEM_KEY = "item";

    private String item;

    private TextView view;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(String item) {
        Bundle args = new Bundle();
        args.putString(ITEM_KEY, item);
        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = getArguments().getString(ITEM_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = (TextView) inflater.inflate(R.layout.fragment_details, container, false);
        view.setText(item);
        return view;
    }

    public void changeItem(String item){
        this.item = item;
        view.setText(item);
    }
}
