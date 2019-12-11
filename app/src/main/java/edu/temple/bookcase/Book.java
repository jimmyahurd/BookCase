package edu.temple.bookcase;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.Serializable;

public class Book implements Parcelable, Serializable {
    private int id;
    private String title;
    private String author;
    private int duration;
    private int published;
    private String coverURL;
    private boolean downloaded;
    private int progress;
    private File audio;

    public Book(int id, String title, String author, int duration, int published, String coverURL, boolean downloaded){
        this.id = id;
        this.title = title;
        this.author = author;
        this.duration = duration;
        this.published = published;
        this.coverURL = coverURL;
        this.downloaded = downloaded;
        this.progress = progress;
    }

    public Book(JSONObject book, boolean fromFile){
        try {
            //Log.d("book", "book " + book.getString("title") + " obtained");
            id = book.getInt("book_id");
            title = book.getString("title");
            author = book.getString("author");
            duration = book.getInt("duration");
            published = book.getInt("published");
            coverURL = book.getString("cover_url");
            if(fromFile){
                downloaded = book.getBoolean("downloaded");
                progress = book.getInt("progress");
            }else{
                progress = 0;
                downloaded = false;
            }
        }catch(Exception e){e.printStackTrace();}
    }

    protected Book(Parcel in) {
        id = in.readInt();
        title = in.readString();
        author = in.readString();
        duration = in.readInt();
        published = in.readInt();
        coverURL = in.readString();
        downloaded = in.readByte() != 0;
        progress = in.readInt();
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

    public boolean isDownloaded(){
        return downloaded;
    }

    public void setAudio(File audio){
        downloaded = true;
        this.audio = audio;
    }

    public void deleteAudio(){
        downloaded = false;
        audio = null;
    }

    public File getAudio(){ return audio;}

    public int getProgress(){ return progress;}

    public void setProgress(int progress){
        this.progress = progress;
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
        dest.writeByte((byte)(downloaded ? 1:0));
        dest.writeInt(progress);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject toReturn = new JSONObject();
        //Log.d("BOOK", "making JSON object");
        toReturn.put("book_id", id);
        toReturn.put("title", title);
        toReturn.put("author", author);
        toReturn.put("duration", duration);
        toReturn.put("published", published);
        toReturn.put("cover_url", coverURL);
        toReturn.put("downloaded", downloaded);
        toReturn.put("progress", progress);
        //Log.d("BOOK", "made JSON object");
        //Log.d("made", toReturn.toString());
        return toReturn;
    }
}
