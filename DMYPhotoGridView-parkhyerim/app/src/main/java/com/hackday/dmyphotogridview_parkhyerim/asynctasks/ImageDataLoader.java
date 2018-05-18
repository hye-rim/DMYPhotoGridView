package com.hackday.dmyphotogridview_parkhyerim.asynctasks;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifImageDirectory;
import com.hackday.dmyphotogridview_parkhyerim.models.ExifImageData;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hyerim on 2018. 5. 17....
 */
public class ImageDataLoader extends AsyncTaskLoader<ArrayList<ExifImageData>> {
    private static final String TAG = ImageDataLoader.class.getSimpleName();
    private static final String[] IMAGE_PROJECTION = new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};

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

    @Override
    public ArrayList<ExifImageData> loadInBackground() {
        ArrayList<ExifImageData> imageList = loadImages(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, MediaStore.Images.Media.DATE_TAKEN, IMAGE_PROJECTION[1], IMAGE_PROJECTION[0]);

        try {
            imageList = getDateTime(imageList);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return imageList;
    }


    private ArrayList<ExifImageData> loadImages(Uri contentUri, String[] projection, String orderBy, String idCol, String dataCol) {
        final ArrayList<ExifImageData> data = new ArrayList<ExifImageData>();

        Cursor cursor = getContext().getContentResolver().query(contentUri, projection, null, null, orderBy + " DESC LIMIT 1000");//orderBy + " DESC"

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

    private ArrayList<ExifImageData> getDateTime(ArrayList<ExifImageData> imageList) throws ParseException {
        for (ExifImageData image : imageList) {
            Date date = extractExifDateTime(image.path);
            image.dateTime = String.valueOf(date);
            image.dateTimeNum = image.dateTime.replaceAll("[^0-9]", "");
        }

        return imageList;

    }
    private Date extractExifDateTime(String imagePath) throws ParseException {
//        Log.d("exif", "Attempting to extract EXIF date/time from image at " + imagePath);
        Date datetime = new Date(0); // or initialize to null, if you prefer
        try {
            Metadata metadata = JpegMetadataReader.readMetadata(new File(imagePath));
            // these are listed in order of preference
            int datetimeTag = ExifImageDirectory.TAG_DATETIME;

            for (Directory directory : metadata.getDirectories()) {
                if (directory.containsTag(datetimeTag)) {
                    SimpleDateFormat exifDatetimeFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
                    datetime = exifDatetimeFormat.parse(directory.getString(datetimeTag));
                    break;
                }
            }
        } catch (Exception e) {
            Log.w("exif", "Unable to extract EXIF metadata from image at " + imagePath, e);
        }

        return datetime;
    }

    private void registerContentObserver() {
        if (!observerRegistered) {
            ContentResolver cr = getContext().getContentResolver();
            cr.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, forceLoadContentObserver);

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
