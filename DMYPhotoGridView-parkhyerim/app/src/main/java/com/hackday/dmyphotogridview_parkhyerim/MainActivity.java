package com.hackday.dmyphotogridview_parkhyerim;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;

public class MainActivity extends BaseActivity{
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static int[] ROW_COUNT = {3, 5, 7};

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        //TODO : glide

        //TODO : grid view
        //TODO : reycler view

        //TODO : imageData class
        //TODO : load image
        //TODO : image list sorting
        //TODO : grouping
        //TODO : pinch zoom

    }

    private void init(){
        mContext = getApplicationContext();

        checkPermission();
        Glide.get(this).setMemoryCategory(MemoryCategory.HIGH);
    }

    @Override
    protected void onPermissionGranted() {
        super.onPermissionGranted();


    }
}
