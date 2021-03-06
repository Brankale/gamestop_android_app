package com.fermimn.gamewishlist.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.fermimn.gamewishlist.R;
import com.fermimn.gamewishlist.fragments.SearchFragment;
import com.fermimn.gamewishlist.fragments.WishlistFragment;

public class MainActivity extends AppCompatActivity {

    @SuppressWarnings("unused")
    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageButton mImageButton;
    private SearchFragment mSearchSection;
    private WishlistFragment mWishListSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set action bar
        // DOCS: https://developer.android.com/training/appbar/setting-up
        // DOCS: https://developer.android.com/training/appbar/actions
        Toolbar toolbar = findViewById(R.id.action_bar);
        toolbar.setTitle( getString(R.string.section_wishlist) );
        setSupportActionBar(toolbar);

        // set button icon
        mImageButton = findViewById(R.id.button);
        mImageButton.setImageResource(R.drawable.ic_search_black_24dp);

        // add sections
        mSearchSection = new SearchFragment();
        mWishListSection = new WishlistFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container, mWishListSection, "wishlist_section");
        transaction.add(R.id.container, mSearchSection, "search_section");
        transaction.hide(mSearchSection);
        transaction.commit();
    }


    @Override
    public void onBackPressed() {
        if (mWishListSection.isHidden()) {
            switchToWishlist();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mWishListSection.isHidden()) {
                switchToWishlist();
            } else {
                finish();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.action_search:
                if (mSearchSection.isHidden()) {
                    switchToSearch();
                }
                return true;

            case R.id.action_open_website:
                Intent browserIntent =
                        new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.site_gamestop)));
                startActivity(browserIntent);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Hide wishlist and show search section and vice-versa
     * @param view the caller view
     */
    @SuppressWarnings("unused")
    public void sectionSwitch(View view) {
        // if search section is hidden
        if (mSearchSection.isHidden()) {
            switchToSearch();
        } else {
            switchToWishlist();
        }
    }

    private void switchToSearch() {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // show search section
        transaction.hide(mWishListSection);
        transaction.show(mSearchSection);

        // set action bar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle( getString(R.string.section_search) ) ;
        }

        // set floating button image
        mImageButton.setImageResource(R.drawable.ic_videogame_asset_black_24dp);

        transaction.commit();
    }

    private void switchToWishlist() {

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // show wishlist section
        transaction.show(mWishListSection);
        transaction.hide(mSearchSection);

        // set action bar title
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle( getString(R.string.section_wishlist) ) ;
        }

        // set floating button image
        mImageButton.setImageResource(R.drawable.ic_search_black_24dp);

        transaction.commit();
    }

}
