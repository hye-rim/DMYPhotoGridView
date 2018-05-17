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
    public String dateTime; //2018:05:23 10:15
    public String dateTimeNum; //201805231015
    public int year; //2018
    public int month; //5
    public int day; //23
    public String path;

    public ExifImageData(long rowId, Uri uri, String path) {
        this.rowId = rowId;
        this.uri = uri;
        this.path = path;
    }

    public ExifImageData(long rowId, Uri uri, String dateTime, String dateTimeNum, int year, int month, int day) {
        this.rowId = rowId;
        this.uri = uri;
        this.dateTime = dateTime;
        this.dateTimeNum = dateTimeNum;
        this.year = year;
        this.month = month;
        this.day = day;
    }

    public ExifImageData(Parcel in) {
        rowId = in.readLong();
        uri = Uri.parse(in.readString());
        dateTime = in.readString();
        dateTimeNum = in.readString();
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
        path = in.readString();
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(rowId);
        parcel.writeString(uri.toString());
        parcel.writeString(dateTime);
        parcel.writeString(dateTimeNum);
        parcel.writeInt(year);
        parcel.writeInt(month);
        parcel.writeInt(day);
        parcel.writeString(path);
    }

    @Override
    public String toString() {
        return "ExifImageData{" +
                "uri=" + uri +
                ", dateTime='" + dateTime + '\'' +
                ", dateTimeNum=" + dateTimeNum +
                ", year=" + year +
                ", month=" + month +
                ", day=" + day +
                ", path=" + path +
                '}';
    }
}