package com.hackday.dmyphotogridview_parkhyerim.asynctasks;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;

import com.hackday.dmyphotogridview_parkhyerim.models.ExifImageData;

import java.util.ArrayList;

/**
 * Created by hyerim on 2018. 5. 17....
 */
public class ImageDataLoader extends AsyncTaskLoader<ArrayList<ExifImageData>> {
    private static final String TAG = ImageDataLoader.class.getSimpleName();
    private static final String[] IMAGE_PROJECTION = new String[]{MediaStore.Images.Thumbnails.DATA, MediaStore.Images.Thumbnails._ID};

    private Context mContext;
    private ArrayList<ExifImageData> cached;
    private boolean observerRegistered = false;
    private final ForceLoadContentObserver forceLoadContentObserver = new ForceLoadContentObserver();

    public ImageDataLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void deliverResult(ArrayList<ExifImageData> data) {
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

    @SuppressLint("RestrictedApi")
    @Override
    public ArrayList<ExifImageData> loadInBackground() {
        ArrayList<ExifImageData> imageList = loadImages(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, MediaStore.Images.Media.DATE_TAKEN, IMAGE_PROJECTION[1], IMAGE_PROJECTION[0] );

//        Collections.sort(imageList, new Comparator<ExifImageData>() {
//            @Override
//            public int compare(ExifImageData mediaStoreData, ExifImageData mediaStoreData2) {
//                return Long.valueOf(mediaStoreData2.dateTime).compareTo(Long.valueOf(mediaStoreData.dateTime));
//            }
//        });

        return imageList;
    }


    private ArrayList<ExifImageData> loadImages(Uri contentUri, String[] projection, String orderBy, String idCol, String dataCol) {
        final ArrayList<ExifImageData> data = new ArrayList<ExifImageData>();

        Cursor cursor = getContext().getContentResolver().query(contentUri, projection, null, null, orderBy + " DESC" + " LIMIT 1000");

        if (cursor == null) {
            return data;
        }

        try {
            final int idColNum = cursor.getColumnIndexOrThrow(idCol);
            final int dataColNum = cursor.getColumnIndexOrThrow(dataCol);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColNum);
                String path = cursor.getString(dataColNum);
                data.add(new ExifImageData(id, Uri.withAppendedPath(contentUri, Long.toString(id)), path));
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
