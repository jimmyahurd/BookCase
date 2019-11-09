package edu.temple.bookcase;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListFragmentAdapter extends RecyclerView.Adapter<ListFragmentAdapter.ViewHolder> {
    private ArrayList<Book> books;

    private static ClickListener clickListener;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public TextView textView;
        public int position;

        public ViewHolder(TextView view){
            super(view);
            textView = view;
            this.position = position;
        }

        public void bind(String item, int position){
            textView.setText(item);
            textView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    clickListener.onItemClick(getAdapterPosition(), v);
                }
            });
            this.position = position;
        }
    }

    public ListFragmentAdapter(ArrayList<Book> books){
        this.books = books;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView view = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_viewholder, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(books.get(position).getTitle(), position);
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public void setOnItemClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }

    public interface ClickListener{
        void onItemClick(int position, View v);
    }
}
