package com.hackday.dmyphotogridview_parkhyerim.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hyerim on 2018. 5. 17....
 */
public class ExifImageData implements Parcelable {
    public static final Creator<ExifImageData> CREATOR = new Creator<ExifImageData>() {
        @Override
        public ExifImageData createFromParcel(Parcel parcel) {
            return new ExifImageData(parcel);
        }

        @Override
        public ExifImageData[] newArray(int i) {
            return new ExifImageData[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    public final long rowId;
    public final Uri uri;
    public final String dateTime; //2018:05:23 10:15
    public final String dateTimeOriginal;
    public final String dateTimeDigitized;
    public long dateTimeLong; //201805231015
    public int year; //2018
    public int month; //5
    public int day; //23

    public ExifImageData(long rowId, Uri uri, String dateTime, String dateTimeOriginal, String dateTimeDigitized, long dateTimeLong, int year, int month, int day) {
        this.rowId = rowId;
        this.uri = uri;
        this.dateTime = dateTime;
        this.dateTimeDigitized = dateTimeDigitized;
        this.dateTimeOriginal = dateTimeOriginal;
        this.dateTimeLong = dateTimeLong;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public ExifImageData(Parcel in) {
        rowId = in.readLong();
        uri = Uri.parse(in.readString());
        dateTime = in.readString();
        dateTimeOriginal = in.readString();
        dateTimeDigitized = in.readString();
        dateTimeLong = in.readLong();
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(rowId);
        parcel.writeString(uri.toString());
        parcel.writeString(dateTime);
        parcel.writeString(dateTimeOriginal);
        parcel.writeString(dateTimeDigitized);
        parcel.writeLong(dateTimeLong);
        parcel.writeInt(year);
        parcel.writeInt(month);
        parcel.writeInt(day);
    }

    @Override
    public String toString() {
        return "ExifImageData{" +
                "uri=" + uri +
                ", dateTime='" + dateTime + '\'' +
                ", dateTimeOriginal='" + dateTimeOriginal + '\'' +
                ", dateTimeDigitized='" + dateTimeDigitized + '\'' +
                ", dateTimeLong=" + dateTimeLong +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                '}';
    }
}