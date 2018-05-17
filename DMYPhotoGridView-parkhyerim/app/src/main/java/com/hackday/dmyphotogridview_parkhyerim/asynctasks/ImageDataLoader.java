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
import java.util.List;

/**
 * Created by hyerim on 2018. 5. 17....
 */
public class ImageDataLoader extends AsyncTaskLoader<List<ExifImageData>> {
    private static final String TAG = ImageDataLoader.class.getSimpleName();
    private static final String[] IMAGE_PROJECTION = new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.ImageColumns._ID};

    private Context mContext;
    private List<ExifImageData> cached;
    private boolean observerRegistered = false;
    private final ForceLoadContentObserver forceLoadContentObserver = new ForceLoadContentObserver();

    public ImageDataLoader(Context context) {
        super(context);
        mContext = context;
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

    @SuppressLint("RestrictedApi")
    @Override
    public List<ExifImageData> loadInBackground() {
        List<ExifImageData> imageList = loadImages(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, MediaStore.Images.Media.DATE_TAKEN, IMAGE_PROJECTION[1], IMAGE_PROJECTION[0] );

//        for (ExifImageData image : imageList) {
//            try {
//                ExifInterface exifInterface = new ExifInterface(image.path);
//
//                if (exifInterface != null) {
//                    image.dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
//                    image.dateTimeNum = image.dateTime.replaceAll("[^0-9]", "");
//                    image.year = Integer.parseInt(image.dateTimeNum.substring(0, 4));
//                    image.month = Integer.parseInt(image.dateTimeNum.substring(5, 6));
//                    image.day = Integer.parseInt(image.dateTimeNum.substring(7, 8));
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }

//        Collections.sort(imageList, new Comparator<ExifImageData>() {
//            @Override
//            public int compare(ExifImageData mediaStoreData, ExifImageData mediaStoreData2) {
//                return Long.valueOf(mediaStoreData2.dateTime).compareTo(Long.valueOf(mediaStoreData.dateTime));
//            }
//        });

        return imageList;
    }


    private List<ExifImageData> loadImages(Uri contentUri, String[] projection, String orderBy, String idCol, String dataCol) {
        final List<ExifImageData> data = new ArrayList<ExifImageData>();

        Cursor cursor = getContext().getContentResolver().query(contentUri, projection, null, null, orderBy + " DESC" + " LIMIT 200");

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
