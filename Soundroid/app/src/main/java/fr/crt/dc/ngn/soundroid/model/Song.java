package fr.crt.dc.ngn.soundroid.model;

import android.graphics.Bitmap;
import android.media.Image;

import java.util.Arrays;
import java.util.Locale;

/**
 * Created by CRETE JONATHAN on 02/04/2020.
 */
public class Song {
    private final long ID;
    private final String title;
    private final String artist;
    private final long duration;
    private final Bitmap artwork;
    private final String style;
    private final String album;
    private  int countSongPlayed;
    private String tag;
    private int rating;
    private String link;
    private byte[] footprint;

    public Song(long ID, String title, String artist, long duration, Bitmap artwork, String style, String album, String link) {
        this.ID = ID;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.artwork = artwork;
        this.style = style;
        this.album = album;
        this.countSongPlayed = 0;
        this.tag = null;
        this.rating = 0;
        this.link = link;
    }

    public Song(long ID, String title, String artist, long duration, Bitmap artwork, String style, String album, String link, byte[] footprint) {
        this.ID = ID;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.artwork = artwork;
        this.style = style;
        this.album = album;
        this.countSongPlayed = 0;
        this.tag = null;
        this.rating = 0;
        this.link = link;
        this.footprint = footprint;
    }




    public static String convertDuration(long duration){
        long minutes = (duration / 1000 ) / 60;
        long seconds = (duration / 1000 ) % 60;
        // met en forme -> EX : 3:03
        return String.format(Locale.getDefault(),"%d:%02d", minutes, seconds);
    }

    public String getLink() {
        return link;
    }

    public long getID() {
        return ID;
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

    public byte[] getFootprint() {
        return footprint;
    }

    public void setFootprint(byte[] footprint) {
        this.footprint = footprint;
    }

    @Override
    public String toString() {
        return "Song{" +
                "ID=" + ID +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", duration=" + duration +
                ", artwork=" + artwork +
                ", style='" + style + '\'' +
                ", album='" + album + '\'' +
                ", countSongPlayed=" + countSongPlayed +
                ", tag='" + tag + '\'' +
                ", rating=" + rating +
                ", link='" + link + '\'' +
                ", footprint=" + Arrays.toString(footprint) +
                '}';
    }
}
