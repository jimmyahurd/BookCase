package edu.temple.bookcase;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class ListFragment extends Fragment {
    private static final String LIST_KEY = "list";
    private ArrayList<String> list;

    private itemSelectedInterface parent;

    public ListFragment() {
        // Required empty public constructor
    }

    public static ListFragment newInstance(ArrayList<String> items) {
        ListFragment fragment = new ListFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(LIST_KEY, items);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            list = getArguments().getStringArrayList(LIST_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RecyclerView view = (RecyclerView) inflater.inflate(R.layout.fragment_list, container, false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager((Context) parent);
        view.setLayoutManager(layoutManager);

        ListFragmentAdapter adapter = new ListFragmentAdapter(list);
        adapter.setOnItemClickListener(new ListFragmentAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                parent.itemSelected(position);
            }
        });
        view.setAdapter(adapter);
        view.addItemDecoration(new DividerItemDecoration((Context) parent, DividerItemDecoration.VERTICAL));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof itemSelectedInterface) {
            parent = (itemSelectedInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement itemSelectedInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parent = null;
    }

    public interface itemSelectedInterface {
        void itemSelected(int position);
    }
}
