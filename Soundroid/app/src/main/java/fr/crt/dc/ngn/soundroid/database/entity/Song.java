package fr.crt.dc.ngn.soundroid.database.entity;

import java.util.Locale;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**s
 * Created by CRETE JONATHAN on 01/05/2020.
 */

@Entity(foreignKeys = @ForeignKey(entity = Playlist.class, parentColumns = "id", childColumns = "playlist_id"), indices = {
        @Index("title"), @Index("playlist_id")
})
public class Song {

    @PrimaryKey
    private long id;

    @ColumnInfo(name= "title")
    private String title;

    @ColumnInfo(name = "artist")
    private String artist;

    @ColumnInfo(name="duration")
    private long duration;

    @ColumnInfo(name = "artwork_url")
    private String artworkUrl;

    @ColumnInfo(name = "style")
    private  String style;

    @ColumnInfo(name = "album")
    private String album;

    @ColumnInfo(name = "countSongPlayed")
    private int countSongPlayed;

    @ColumnInfo(name = "tag")
    private String tag;

    @ColumnInfo(name = "rating")
    private int rating;

    @ColumnInfo(name = "link")
    private String link;

    @ColumnInfo(name = "footprint")
    private String footprint;

    @ColumnInfo(name = "playlist_id")
    private long playlistId;

    public Song() {}

    public Song(long ID, String title, String artist, long duration, String artwork, String style, String album, String link, String footprint) {
        this.id = ID;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.artworkUrl = artwork;
        this.style = style;
        this.album = album;
        this.countSongPlayed = 0;
        this.tag = null;
        this.rating = 0;
        this.link = link;
        this.footprint = footprint;
        this.playlistId = 123;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getArtworkUrl() {
        return artworkUrl;
    }

    public void setArtworkUrl(String artworkUrl) {
        this.artworkUrl = artworkUrl;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
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

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getFootprint() {
        return footprint;
    }

    public void setFootprint(String footprint) {
        this.footprint = footprint;
    }

    public long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(long playlistId) {
        this.playlistId = playlistId;
    }

    public static String convertDuration(long duration){
        long minutes = (duration / 1000 ) / 60;
        long seconds = (duration / 1000 ) % 60;
        // met en forme -> EX : 3:03
        return String.format(Locale.getDefault(),"%d:%02d", minutes, seconds);
    }
    
    public static Song[] populateData() {
        return new Song[] {
                new Song(1, "Billie Jean", "Michael Jackson", 2503, "rep/artwork", "pop", "King of pop", "rep/...",  "89+79gs76g"),
                new Song(2, "Beat it", "Michael Jackson", 2553, "rep/artwork", "pop", "King of pop", "rep/...",  "89+79gs76g"),
                new Song(3, "Thriller", "Michael Jackson", 2793, "rep/artwork", "pop", "King of pop", "rep/...",  "89+79gs76g")
        };
    }





     

}
