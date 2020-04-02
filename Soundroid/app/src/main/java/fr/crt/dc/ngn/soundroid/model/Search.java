package fr.crt.dc.ngn.soundroid.model;

public class Search {
    private final String title;
    private final String artist;
    private String tag;
    private final String album;
    private final String style;
    private int rating;


    public Search(String title, String artist, String tag, String album, String style, int rating) {

        this.title = title;
        this.artist = artist;
        this.tag = tag;
        this.album = album;
        this.style = style;
        this.rating = rating;
    }
}
