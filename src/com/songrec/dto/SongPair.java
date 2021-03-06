package com.songrec.dto;

import com.songrec.utils.HashingX;
import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class SongPair implements WritableComparable<SongPair> {
    private int firstSongId;
    private int secondSongId;

    public SongPair() {
    }

    public SongPair(int firstSongId, int secondSongId) {
        this.firstSongId = firstSongId;
        this.secondSongId = secondSongId;
    }

    public int firstSongId() {
        return firstSongId;
    }

    public int secondSongId() {
        return secondSongId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SongPair songPair = (SongPair) o;

        if (firstSongId != songPair.firstSongId) return false;
        if (secondSongId != songPair.secondSongId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sha1Hashcode(firstSongId);
        result = 31 * result + sha1Hashcode(secondSongId);
        return result;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(firstSongId);
        out.writeInt(secondSongId);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        firstSongId = in.readInt();
        secondSongId = in.readInt();
    }

    @Override
    public int compareTo(SongPair songPair) {
        int result = compare(firstSongId, songPair.firstSongId);
        if(result != 0) return result;
        return compare(secondSongId, songPair.secondSongId) ;
    }

    private int compare(int one, int another) {
        return one > another ? 1 : (one < another ? -1 : 0);
    }

    @Override
    public String toString() {
        return firstSongId + "," + secondSongId;
    }

    private int sha1Hashcode(int songId) {
        return HashingX.hash(songId);
    }
}
