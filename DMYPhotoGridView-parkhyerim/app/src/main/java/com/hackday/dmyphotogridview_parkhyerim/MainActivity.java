package com.hackday.dmyphotogridview_parkhyerim;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.Preconditions;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifImageDirectory;
import com.hackday.dmyphotogridview_parkhyerim.adapters.RecyclerAdapter;
import com.hackday.dmyphotogridview_parkhyerim.asynctasks.ImageDataLoader;
import com.hackday.dmyphotogridview_parkhyerim.models.ExifImageData;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<ArrayList<ExifImageData>> {
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static int[] ROW_COUNT = {3, 5, 7};

    private Context mContext;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private RecyclerAdapter mRecyclerAdapter;

    private int mScreenWidth;
    private int mNowRowCountIndex = 0;
    private boolean isChangeRowCnt = false;

    private ScaleGestureDetector mScaleGestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        //TODO : grouping

    }

    private void init() {
        mContext = getApplicationContext();
        mScreenWidth = getScreenWidth(mContext);

        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        mLayoutManager = new GridLayoutManager(mContext, ROW_COUNT[mNowRowCountIndex]);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        checkPermission();

//        Glide.get(this).setMemoryCategory(MemoryCategory.HIGH);
        Glide.get(this);
        getSupportLoaderManager().initLoader(R.id.loader_id_media_store_data, null, this);

        setPinch();
    }

    @Override
    protected void onPermissionGranted() {
        super.onPermissionGranted();
    }

    private void setPinch() {
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (detector.getCurrentSpan() > 200 && detector.getTimeDelta() > 200) {
                    if (detector.getCurrentSpan() - detector.getPreviousSpan() < -1) {
                        if (mNowRowCountIndex > 0) {
                            isChangeRowCnt = true;
                            mNowRowCountIndex--;
                            mLayoutManager = new GridLayoutManager(mContext, ROW_COUNT[mNowRowCountIndex]);
                            mRecyclerView.setLayoutManager(mLayoutManager);
                            isChangeRowCnt = true;
                            return true;
                        }
                    } else if (detector.getCurrentSpan() - detector.getPreviousSpan() > 1) {
                        if (mNowRowCountIndex < 2) {
                            isChangeRowCnt = true;
                            mNowRowCountIndex++;
                            mLayoutManager = new GridLayoutManager(mContext, ROW_COUNT[mNowRowCountIndex]);
                            mRecyclerView.setLayoutManager(mLayoutManager);
                            return true;
                        }
                    }
                }
                return false;
            }
        });

        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mScaleGestureDetector.onTouchEvent(event);
//                v.getRootView().
////                v.getLayoutParams().width = mScreenWidth / ROW_COUNT[mNowRowCountIndex];

                return false;
            }
        });

        if (isChangeRowCnt) {
            getSupportLoaderManager().restartLoader(R.id.loader_id_media_store_data, null, this);
            isChangeRowCnt = false;
        }

    }

    @SuppressWarnings("deprecation")
    private static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = Preconditions.checkNotNull(wm).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    @Override
    public Loader<ArrayList<ExifImageData>> onCreateLoader(int i, Bundle bundle) {
        return new ImageDataLoader(mContext);
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<ExifImageData>> loader, ArrayList<ExifImageData> imageData) {
        RequestBuilder<Drawable> requestBuilder = Glide.with(this).asDrawable();

//        for(ExifImageData image : imageData){
//            Date date = extractExifDateTime(image.path);
//            image.dateTime = String.valueOf(date);
//            image.dateTimeNum = image.dateTime.replaceAll("[^0-9]", "");
//        }

        imageData = grouping(imageData);

        mRecyclerAdapter = new RecyclerAdapter(mContext, imageData, requestBuilder, mScreenWidth / ROW_COUNT[mNowRowCountIndex]);
        RecyclerViewPreloader<ExifImageData> recyclerViewPreloader = new RecyclerViewPreloader<>(Glide.with(this), mRecyclerAdapter, mRecyclerAdapter, 3);
        mRecyclerView.addOnScrollListener(recyclerViewPreloader);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    private ArrayList<ExifImageData> grouping(ArrayList<ExifImageData> imageData) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN);
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy년 MM월", Locale.KOREAN);
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy년", Locale.KOREAN);

        return imageData;
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<ExifImageData>> loader) {

    }

    private Date extractExifDateTime(String imagePath) {
//        Log.d("exif", "Attempting to extract EXIF date/time from image at " + imagePath);
        Date datetime = new Date(0); // or initialize to null, if you prefer
        try {
            Metadata metadata = JpegMetadataReader.readMetadata(new File(imagePath));
            // these are listed in order of preference
            int[] datetimeTags = new int[] { ExifImageDirectory.TAG_DATETIME_ORIGINAL,
                    ExifImageDirectory.TAG_DATETIME,
                    ExifImageDirectory.TAG_DATETIME_DIGITIZED };

            for (Directory directory : metadata.getDirectories()) {
                for (int tag : datetimeTags) {
                    if (directory.containsTag(tag)) {
//                        Log.d("exif", "Using tag " + directory.getTagName(tag) + " for timestamp");
                        SimpleDateFormat exifDatetimeFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault());
                        datetime = exifDatetimeFormat.parse(directory.getString(tag));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.w("exif", "Unable to extract EXIF metadata from image at " + imagePath, e);
        }

        return datetime;
    }

}
