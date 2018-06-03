package com.hackday.dmyphotogridview_parkhyerim.asynctasks;

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
import com.hackday.dmyphotogridview_parkhyerim.models.GroupingImageData;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

/**
 * Created by hyerim on 2018. 5. 17....
 */
public class ImageDataLoader extends AsyncTaskLoader<GroupingImageData> {
    private static final String TAG = ImageDataLoader.class.getSimpleName();
    private static final String[] IMAGE_PROJECTION = new String[]{MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};

    final SimpleDateFormat exifDatetimeFormatDefault = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
    final SimpleDateFormat exifDatetimeFormatSSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault());
    final SimpleDateFormat exifDatetimeFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
    final SimpleDateFormat appDateformat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN);

    private Context mContext;
    private GroupingImageData mCached;
    private boolean isObserverRegistered = false;
    private final ForceLoadContentObserver mForceLoadContentObserver = new ForceLoadContentObserver();

    public ImageDataLoader(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void deliverResult(GroupingImageData data) {
        if (!isReset() && isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        if (mCached != null) {
            deliverResult(mCached);
        }
        if (takeContentChanged() || mCached == null) {
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
        mCached = null;
        unregisterContentObserver();
    }

    @Override
    protected void onAbandon() {
        super.onAbandon();
        unregisterContentObserver();
    }

    @Override
    public GroupingImageData loadInBackground() {
        ArrayList<ExifImageData> imageList = loadImages(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, MediaStore.Images.Media.DATE_TAKEN, IMAGE_PROJECTION[1], IMAGE_PROJECTION[0]);

        try {
            imageList = getDateTime(imageList);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        GroupingImageData groupingImageList = grouping(imageList);

        //TODO: SORTING

        return groupingImageList;
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
                if (!path.contains(".png") && !path.contains(".gif")) { //png, gif X
                    data.add(new ExifImageData(id, Uri.withAppendedPath(contentUri, Long.toString(id)), path));
                }
            }
        } finally {
            cursor.close();
        }

        return data;
    }

    private ArrayList<ExifImageData> getDateTime(ArrayList<ExifImageData> imageList) throws ParseException {
        for (ExifImageData image : imageList) {
            String date = extractExifDateTime(image.path);
            image.dateTime = date;
        }

        return imageList;
    }

    private String extractExifDateTime(String imagePath) throws ParseException {
        Date datetime = new Date(0); // or initialize to null, if you prefer
        String formatdate = " ";
        try {
            Metadata metadata = JpegMetadataReader.readMetadata(new File(imagePath));
            int[] datetimeTags = new int[]{ExifImageDirectory.TAG_DATETIME_ORIGINAL,
                    ExifImageDirectory.TAG_DATETIME,
                    ExifImageDirectory.TAG_DATETIME_DIGITIZED};

            for (Directory directory : metadata.getDirectories()) {
                for (int tag : datetimeTags) {
                    if (directory.containsTag(tag)) {
                        String date = directory.getString(tag);
                        if (date.isEmpty() || date.length() <= 1) {
                            continue;
                        }

                        //date format EEE MMM dd HH:mm:ss z yyyy / Locale.ENGLISH
                        if (date.contains("GMT")) {
                            exifDatetimeFormatDefault.setTimeZone(TimeZone.getTimeZone("GMT"));
                            datetime = exifDatetimeFormatDefault.parse(directory.getString(tag));
                        }

                        //date format HH:mm:ss:SSS
                        else if (date.length() > 20) {
                            datetime = exifDatetimeFormatSSS.parse(directory.getString(tag));
                        }

                        //date format HH:mm:ss
                        else {
                            datetime = exifDatetimeFormat.parse(directory.getString(tag));
                        }

                        formatdate = appDateformat.format(datetime);

                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.w("exif", "Unable to extract EXIF metadata from image at " + imagePath, e);
        }

        return formatdate;
    }

    private GroupingImageData grouping(ArrayList<ExifImageData> imageData) {
        Map<String, ArrayList<ExifImageData>> dailyGroup = new HashMap<String, ArrayList<ExifImageData>>();
        Map<String, ArrayList<ExifImageData>> monthlyGroup = new HashMap<String, ArrayList<ExifImageData>>();
        Map<String, ArrayList<ExifImageData>> yearGroup = new HashMap<String, ArrayList<ExifImageData>>();

        String nowDate;
        for (ExifImageData image : imageData) {
            String date = image.dateTime;


            //daily - 0000년 00월 00일
            if (dailyGroup.containsKey(date)) {
                dailyGroup.get(date).add(image);
            } else {
                ArrayList<ExifImageData> imageList = new ArrayList<ExifImageData>();
                imageList.add(image);
                dailyGroup.put(date, imageList);
            }

            //month - 0000년 00월
            nowDate = new String();
            if (!date.isEmpty() && date.length() > 1) {
                nowDate = date.substring(0, 9);
            }
            if (monthlyGroup.containsKey(nowDate)) {
                monthlyGroup.get(nowDate).add(image);
            } else {
                ArrayList<ExifImageData> imageList = new ArrayList<ExifImageData>();
                imageList.add(image);
                monthlyGroup.put(nowDate, imageList);
            }

            //year - 0000년
            nowDate = new String();
            if (!date.isEmpty() && date.length() > 1) {
                nowDate = date.substring(0, 5);
            }
            if (yearGroup.containsKey(nowDate)) {
                yearGroup.get(nowDate).add(image);
            } else {
                ArrayList<ExifImageData> imageList = new ArrayList<ExifImageData>();
                imageList.add(image);
                yearGroup.put(nowDate, imageList);
            }
        }

        GroupingImageData groupingImageList = new GroupingImageData(
                sortingImageGroup(dailyGroup),
                sortingImageGroup(monthlyGroup),
                sortingImageGroup(yearGroup));

        return groupingImageList;
    }

    private Map<String, ArrayList<ExifImageData>> sortingImageGroup(final Map<String, ArrayList<ExifImageData>> imageGroup) {
        //최신순으로 sorting

        TreeMap<String, ArrayList<ExifImageData>> sortedMap = new TreeMap<String, ArrayList<ExifImageData>>(Collections.<String>reverseOrder());
        sortedMap.putAll(imageGroup);

        return sortedMap;
    }

    private void registerContentObserver() {
        if (!isObserverRegistered) {
            ContentResolver cr = getContext().getContentResolver();
            cr.registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, mForceLoadContentObserver);

            isObserverRegistered = true;
        }
    }

    private void unregisterContentObserver() {
        if (isObserverRegistered) {
            isObserverRegistered = false;

            getContext().getContentResolver().unregisterContentObserver(mForceLoadContentObserver);
        }
    }
}
