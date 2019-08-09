package com.fermimn.gamewishlist.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.fermimn.gamewishlist.R;
import com.fermimn.gamewishlist.adapters.GalleryAdapter;

import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends FragmentActivity {

    @SuppressWarnings("unused")
    private static final String TAG = GalleryActivity.class.getSimpleName();

    private ViewPager mViewPager;
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        Intent caller = getIntent();
        String[] images = caller.getStringArrayExtra("URIs");

        if (images == null) {
            Log.d(TAG, "Intent content is null");
            return;
        }

        List<Uri> uri = new ArrayList<>();
        for (String image : images) {
            uri.add( Uri.parse(image) );
        }

        // Instantiate a ViewPager and a PagerAdapter
        mViewPager = findViewById(R.id.view_pager);
        pagerAdapter = new GalleryAdapter(getSupportFragmentManager(), uri);
        mViewPager.setAdapter(pagerAdapter);

        if (savedInstanceState != null) {
            mViewPager.setCurrentItem( savedInstanceState.getInt("position") );
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("position", mViewPager.getCurrentItem());
        super.onSaveInstanceState(outState);
    }
}