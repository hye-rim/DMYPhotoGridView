package com.hackday.dmyphotogridview_parkhyerim;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.util.Preconditions;
import com.codewaves.stickyheadergrid.StickyHeaderGridLayoutManager;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifImageDirectory;
import com.hackday.dmyphotogridview_parkhyerim.adapters.RecyclerAdapter;
import com.hackday.dmyphotogridview_parkhyerim.asynctasks.ImageDataLoader;
import com.hackday.dmyphotogridview_parkhyerim.models.ExifImageData;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<ArrayList<ExifImageData>> {
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static int[] ROW_COUNT = {3, 5, 7};

    private Context mContext;
    private RecyclerView mRecyclerView;
    private StickyHeaderGridLayoutManager mGridLayoutManager;
    private RecyclerAdapter mRecyclerAdapter;
    RequestBuilder<Drawable> mRequestBuilder;

    private ScaleGestureDetector mScaleGestureDetector;

    private int mScreenWidth;
    private int mNowRowCountIndex = 0;
    private boolean isChangeRowCnt = false;


    private Map<String, ArrayList<ExifImageData>> dailyGroup = new HashMap<String, ArrayList<ExifImageData>>();
    private Map<String, ArrayList<ExifImageData>> monthlyGroup = new HashMap<String, ArrayList<ExifImageData>>();
    private Map<String, ArrayList<ExifImageData>> yearGroup = new HashMap<String, ArrayList<ExifImageData>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
    }

    private void init() {
        mContext = getApplicationContext();
        mScreenWidth = getScreenWidth(mContext);

        mRecyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        mGridLayoutManager = new StickyHeaderGridLayoutManager(ROW_COUNT[mNowRowCountIndex]);
        mGridLayoutManager.setHeaderBottomOverlapMargin(getResources().getDimensionPixelSize(R.dimen.header_shadow_size));

        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        Glide.get(this);
        getSupportLoaderManager().initLoader(R.id.loader_id_media_store_data, null, this);
        setPinch();
    }

    @Override
    protected void onPermissionGranted() {
        super.onPermissionGranted();

        init();
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
                            mGridLayoutManager = new StickyHeaderGridLayoutManager(ROW_COUNT[mNowRowCountIndex]);
                            mGridLayoutManager.setHeaderBottomOverlapMargin(getResources().getDimensionPixelSize(R.dimen.header_shadow_size));
                            mRecyclerView.setLayoutManager(mGridLayoutManager);
                            updateAdapter();
                            isChangeRowCnt = true;
                            return true;
                        }
                    } else if (detector.getCurrentSpan() - detector.getPreviousSpan() > 1) {
                        if (mNowRowCountIndex < 2) {
                            isChangeRowCnt = true;
                            mNowRowCountIndex++;
                            mGridLayoutManager = new StickyHeaderGridLayoutManager(ROW_COUNT[mNowRowCountIndex]);
                            mGridLayoutManager.setHeaderBottomOverlapMargin(getResources().getDimensionPixelSize(R.dimen.header_shadow_size));
                            mRecyclerView.setLayoutManager(mGridLayoutManager);
                            updateAdapter();
                            isChangeRowCnt = true;
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
        grouping(imageData);

        updateAdapter();
    }

    private void updateAdapter() {
        mRequestBuilder = Glide.with(this).asDrawable();

        if (mNowRowCountIndex == 0) {
            mRecyclerAdapter = new RecyclerAdapter(mContext, dailyGroup, mRequestBuilder, mScreenWidth / ROW_COUNT[mNowRowCountIndex]);
        } else if (mNowRowCountIndex == 1) {
            mRecyclerAdapter = new RecyclerAdapter(mContext, monthlyGroup, mRequestBuilder, mScreenWidth / ROW_COUNT[mNowRowCountIndex]);
        } else {
            mRecyclerAdapter = new RecyclerAdapter(mContext, yearGroup, mRequestBuilder, mScreenWidth / ROW_COUNT[mNowRowCountIndex]);
        }

//        RecyclerViewPreloader<ExifImageData> recyclerViewPreloader = new RecyclerViewPreloader<>(Glide.with(this), mRecyclerAdapter, mRecyclerAdapter, 3);
//        mRecyclerView.addOnScrollListener(recyclerViewPreloader);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }


    private ArrayList<ExifImageData> grouping(ArrayList<ExifImageData> imageData) {
        for (ExifImageData image : imageData) {
            String nowDate = image.dateTime.substring(0, 10);
            if (dailyGroup.containsKey(nowDate)) {
                dailyGroup.get(nowDate).add(image);
            } else {
                ArrayList<ExifImageData> imageList = new ArrayList<ExifImageData>();
                imageList.add(image);
                dailyGroup.put(nowDate, imageList);
            }
        }

        for (ExifImageData image : imageData) {
            String nowDate = image.dateTime.substring(0, 7);
            if (monthlyGroup.containsKey(nowDate)) {
                monthlyGroup.get(nowDate).add(image);
            } else {
                ArrayList<ExifImageData> imageList = new ArrayList<ExifImageData>();
                imageList.add(image);
                monthlyGroup.put(nowDate, imageList);
            }
        }


        for (ExifImageData image : imageData) {
            String nowDate = image.dateTime.substring(0, 4);
            if (yearGroup.containsKey(nowDate)) {
                yearGroup.get(nowDate).add(image);
            } else {
                ArrayList<ExifImageData> imageList = new ArrayList<ExifImageData>();
                imageList.add(image);
                yearGroup.put(nowDate, imageList);
            }
        }

        return imageData;
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<ExifImageData>> loader) {

    }


}
