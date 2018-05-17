package com.hackday.dmyphotogridview_parkhyerim.asynctasks;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

import com.hackday.dmyphotogridview_parkhyerim.models.ExifImageData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by hyerim on 2018. 5. 17....
 */
public class ImageDataLoader extends AsyncTaskLoader<List<ExifImageData>> {
    private static final String[] IMAGE_PROJECTION =
            new String[]{MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images.ImageColumns._ID};
//                    ExifInterface.TAG_DATETIME, ExifInterface.TAG_DATETIME_ORIGINAL, ExifInterface.TAG_DATETIME_DIGITIZED};

    private List<ExifImageData> cached;
    private boolean observerRegistered = false;
    private final ForceLoadContentObserver forceLoadContentObserver = new ForceLoadContentObserver();

    public ImageDataLoader(Context context) {
        super(context);
    }

    @Override
    public void deliverResult(List<ExifImageData> data) {
        if (!isReset() && isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (cached != null) {
            deliverResult(cached);
        }
        if (takeContentChanged() || cached == null) {
            forceLoad();
        }
        registerContentObserver();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        super.onReset();

        onStopLoading();
        cached = null;
        unregisterContentObserver();
    }

    @Override
    protected void onAbandon() {
        super.onAbandon();
        unregisterContentObserver();
    }

    @Override
    public List<ExifImageData> loadInBackground() {
        List<ExifImageData> data = queryImages();
        Collections.sort(data, new Comparator<ExifImageData>() {
            @Override
            public int compare(ExifImageData mediaStoreData, ExifImageData mediaStoreData2) {
                return Long.valueOf(mediaStoreData2.dateTimeLong).compareTo(mediaStoreData.dateTimeLong);
            }
        });
        return data;
    }

    private List<ExifImageData> queryImages() {
        return query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, MediaStore.Images.ImageColumns.DATE_TAKEN,
                MediaStore.Images.ImageColumns._ID, ExifInterface.TAG_DATETIME, ExifInterface.TAG_DATETIME_ORIGINAL, ExifInterface.TAG_DATETIME_DIGITIZED);
    }

    private List<ExifImageData> query(Uri contentUri, String[] projection, String sortByCol, String idCol, String dateTimeCol, String dateTimeOriginalCol, String dateTimeDigitizedCol) {
        final List<ExifImageData> data = new ArrayList<ExifImageData>();

//        String tempDateTimeLong = dateTimeCol.replaceAll("[^0-9]", "");

        Cursor cursor = getContext().getContentResolver().query(contentUri, projection, null, null, sortByCol + " DESC");

        if (cursor == null) {
            return data;
        }

        try {
            final int idColNum = cursor.getColumnIndexOrThrow(idCol);
            final int dateTimeColNum = cursor.getColumnIndexOrThrow(dateTimeCol);
            final int dateTimeOriginalColNum = cursor.getColumnIndexOrThrow(dateTimeOriginalCol);
            final int dateTimeDigitizedColNum = cursor.getColumnIndexOrThrow(dateTimeDigitizedCol);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColNum);
                String dateTime = cursor.getString(dateTimeColNum);
                String dateTimeOriginal = cursor.getString(dateTimeOriginalColNum);
                String dateTimeDigitized = cursor.getString(dateTimeDigitizedColNum);
                int year = Integer.parseInt(dateTime.substring(0, 3));
                int month = Integer.parseInt(dateTime.substring(5, 6));
                int day = Integer.parseInt(dateTime.substring(8, 9));
                long dateTimeLong = Long.parseLong(dateTime.replaceAll("[^0-9]", ""));

                data.add(new ExifImageData(id, Uri.withAppendedPath(contentUri, Long.toString(id)),
                        dateTime, dateTimeOriginal, dateTimeDigitized, dateTimeLong, year, month, day));
            }
        } finally {
            cursor.close();
        }

        return data;
    }

    private void registerContentObserver() {
        if (!observerRegistered) {
            ContentResolver cr = getContext().getContentResolver();
            cr.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false,
                    forceLoadContentObserver);

            observerRegistered = true;
        }
    }

    private void unregisterContentObserver() {
        if (observerRegistered) {
            observerRegistered = false;

            getContext().getContentResolver().unregisterContentObserver(forceLoadContentObserver);
        }
    }
}
