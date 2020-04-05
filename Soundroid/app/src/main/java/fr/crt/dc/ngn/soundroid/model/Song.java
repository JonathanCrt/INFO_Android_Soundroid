package fr.crt.dc.ngn.soundroid.model;

import android.graphics.Bitmap;
import android.media.Image;

import java.util.Locale;

/**
 * Created by CRETE JONATHAN on 02/04/2020.
 */
public class Song {
    private final String title;
    private final String artist;
    private final long duration;
    private final Bitmap artwork;
    private final String style;
    private final String album;
    private  int countSongPlayed;
    private String tag;
    private int rating;

    public Song(String title, String artist, long duration, Bitmap artwork, String style, String album) {
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.artwork = artwork;
        this.style = style;
        this.album = album;
        this.countSongPlayed = 0;
        this.tag = null;
        this.rating = 0;
    }


    public static String convertDuration(long duration){
        long minutes = (duration / 1000 ) / 60;
        long seconds = (duration / 1000 ) % 60;
        // met en forme -> EX : 3:03
        return String.format(Locale.getDefault(),"%d:%02d", minutes, seconds);
    }


    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public long getDuration() {
        return duration;
    }

    public Bitmap getArtwork() {
        return artwork;
    }

    public int getCountSongPlayed() {
        return countSongPlayed;
    }

    public void setCountSongPlayed(int countSongPlayed) {
        this.countSongPlayed = countSongPlayed;
    }

    public String getTag() {
        return tag;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
