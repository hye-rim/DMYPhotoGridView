package com.hackday.dmyphotogridview_parkhyerim;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        //TODO : 권한체크
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
        checkPermission();
    }

    @Override
    protected void onPermissionGranted() {
        super.onPermissionGranted();


    }
}
