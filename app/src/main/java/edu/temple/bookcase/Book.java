package edu.temple.bookcase;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

public class Book implements Parcelable {
    private int id;
    private String title;
    private String author;
    private int duration;
    private int published;
    private String coverURL;

    public Book(int id, String title, String author, int duration, int published, String coverURL){
        this.id = id;
        this.title = title;
        this.author = author;
        this.duration = duration;
        this.published = published;
        this.coverURL = coverURL;
    }

    public Book(JSONObject book){
        try {
            Log.d("book", "book " + book.getString("title") + " obtained");
            id = book.getInt("book_id");
            title = book.getString("title");
            author = book.getString("author");
            duration = book.getInt("duration");
            published = book.getInt("published");
            coverURL = book.getString("cover_url");
        }catch(Exception e){e.printStackTrace();}
    }

    protected Book(Parcel in) {
        id = in.readInt();
        title = in.readString();
        author = in.readString();
        duration = in.readInt();
        published = in.readInt();
        coverURL = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getDuration() {
        return duration;
    }

    public int getPublished() {
        return published;
    }

    public String getCoverURL() {
        return coverURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeInt(duration);
        dest.writeInt(published);
        dest.writeString(coverURL);
    }
}
