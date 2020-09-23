package edu.temple.bookcase;


import android.content.Context;
import android.os.Bundle;

import org.jetbrains.annotations.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {
    private static final String BOOK_KEY = "book";

    private Book book;

    private BookInterface parent;

    private ImageView cover;
    private TextView title;
    private TextView author;
    private TextView published;

    private static String DisplayTitle = "Title: ";
    private static String DisplayAuthor = "Author: ";
    private static String DisplayPublishedYear = "Published: ";

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
        view.findViewById(R.id.playButton).setOnClickListener(v -> {
            parent.playPressed(book);
        });
        Button downloadDelete = view.findViewById(R.id.downloadDelete);
        downloadDelete.setText(getString(book.isDownloaded()? R.string.deleteButton : R.string.downloadButton));
        downloadDelete.setOnClickListener(v -> {
            downloadDelete.setText(getString(book.isDownloaded()? R.string.downloadButton : R.string.deleteButton));
            parent.downloadOrDelete(book);
        });
        UpdateView();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookInterface) {
            parent = (BookInterface) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement BookInterface");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        parent = null;
    }

    public void DisplayBook(Book book){
        this.book = book;
        UpdateView();
    }

    private void UpdateView(){
        if(book != null) {
            Picasso.get().load(book.getCoverURL()).into(cover);
            title.setText(DisplayTitle + book.getTitle());
            author.setText(DisplayAuthor + book.getAuthor());
            published.setText(DisplayPublishedYear + book.getPublished());
        }
    }

    public interface BookInterface {
        void playPressed(Book book);
        void downloadOrDelete(Book book);
    }
}
