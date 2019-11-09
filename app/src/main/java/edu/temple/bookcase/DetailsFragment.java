package edu.temple.bookcase;


import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {
    private static final String BOOK_KEY = "book";

    private Book book;

    private ImageView cover;
    private TextView title;
    private TextView author;
    private TextView published;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(Book book) {
        Bundle args = new Bundle();
        args.putParcelable(BOOK_KEY, book);
        DetailsFragment fragment = new DetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = (Book) getArguments().getParcelable(BOOK_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        cover = view.findViewById(R.id.cover);
        title = view.findViewById(R.id.title);
        author = view.findViewById(R.id.author);
        published = view.findViewById(R.id.published);
        UpdateView();
        return view;
    }

    public void DisplayBook(Book book){
        this.book = book;
        UpdateView();
    }

    private void UpdateView(){
        if(book != null) {
            Picasso.get().load(book.getCoverURL()).into(cover);
            title.setText("Title: " + book.getTitle());
            author.setText("Author: " + book.getAuthor());
            published.setText("Published: " + book.getPublished());
        }
    }
}
