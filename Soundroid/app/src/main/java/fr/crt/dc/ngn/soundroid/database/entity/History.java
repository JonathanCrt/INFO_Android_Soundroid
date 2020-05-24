package fr.crt.dc.ngn.soundroid.database.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by DA COSTA Mélissa on 24/05/2020.
 */

@Entity(foreignKeys = @ForeignKey(entity = Song.class, parentColumns = "songId", childColumns = "FK_songId"))

@TypeConverters(DateConverters.class)
public class History {

    @PrimaryKey
    private long FK_songId;

    @ColumnInfo(name = "dateLastPlayed")
    private Date dateLastPlayed;

    @ColumnInfo(name = "nbTimesPlayed")
    private long nbTimesPlayed;

    public History() {}

    @Ignore
    public History(long songId) {
        this.FK_songId = songId;
        this.dateLastPlayed = new Date();
        this.nbTimesPlayed = 1;
    }

    public long getFK_songId() {
        return FK_songId;
    }

    public Date getDateLastPlayed() {
        return dateLastPlayed;
    }

    public long getNbTimesPlayed() {
        return nbTimesPlayed;
    }

    @Override
    public String toString() {
        return "Playlist{" +
                "songID=" + FK_songId +
                ", dateLastPlayed ='" + dateLastPlayed + '\'' +
                ", nbTimesPlayed ='" + nbTimesPlayed + '\'' +
                '}';
    }
}
