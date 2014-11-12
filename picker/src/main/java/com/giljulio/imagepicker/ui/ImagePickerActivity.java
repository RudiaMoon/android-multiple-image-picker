package com.giljulio.imagepicker.ui;


import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.giljulio.imagepicker.R;
import com.giljulio.imagepicker.model.Image;
import com.giljulio.imagepicker.utils.ImageInternalFetcher;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


public class ImagePickerActivity extends Activity implements ActionBar.TabListener, View.OnClickListener {

    public static final String TAG_IMAGE_URI = "TAG_IMAGE_URI";
    private static final String TAG = ImagePickerActivity.class.getSimpleName();
    public ImageInternalFetcher mImageFetcher;
    private Set<Image> mSelectedImages;
    private LinearLayout mSelectedImagesContainer;
    private FrameLayout mFrameLayout;
    private TextView mSelectedImageEmptyMessage;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Button mCancelButtonView, mDoneButtonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_image_picker);

//mFrameLayout,mSelectedImagesContainer
        mSelectedImagesContainer = (LinearLayout) findViewById(R.id.selected_photos_container);
        mSelectedImageEmptyMessage = (TextView) findViewById(R.id.selected_photos_empty);
        mFrameLayout = (FrameLayout) findViewById(R.id.selected_photos_container_frame);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mCancelButtonView = (Button) findViewById(R.id.action_btn_cancel);
        mDoneButtonView = (Button) findViewById(R.id.action_btn_done);


        mSelectedImages = new HashSet<Image>();

        mImageFetcher = new ImageInternalFetcher(this, 500);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mCancelButtonView.setOnClickListener(this);
        mDoneButtonView.setOnClickListener(this);

        setupActionBar();


    }

    /**
     * Sets up the action bar, adding view page indicator
     */
    private void setupActionBar() {
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(this));
        }
    }


    public boolean addImage(Image image) {
        if (mSelectedImages.add(image)) {
            View rootView = LayoutInflater.from(ImagePickerActivity.this).inflate(R.layout.list_item_selected_thumbnail, null);
            ImageView thumbnail = (ImageView) rootView.findViewById(R.id.selected_photo);
            rootView.setTag(image.mUri);
            Uri s = image.mUri;
            mImageFetcher.loadImage(image.mUri, thumbnail);
            //기존 추가될때 마다앞에 생성.
            //mSelectedImagesContainer.addView(rootView, 0);
            //새로 추가될 때마다 오른쪽에 이미지 생성.
            mSelectedImagesContainer.addView(rootView, mSelectedImages.size() - 1);

            int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60,
                    getResources().getDisplayMetrics());
            thumbnail.setLayoutParams(new FrameLayout.LayoutParams(px, px));

            if (mSelectedImages.size() == 1) {
                mSelectedImagesContainer.setVisibility(View.VISIBLE);
                mSelectedImageEmptyMessage.setVisibility(View.GONE);
            } else if (mSelectedImages.size() > 5) {
                removeImage(image);
                Toast.makeText(rootView.getContext(), "5장까지 입력가능합니다.", Toast.LENGTH_SHORT).show();

            }
            return true;
        }
        return false;
    }

    public boolean removeImage(Image image) {
        if (mSelectedImages.remove(image)) {
            for (int i = 0; i < mSelectedImagesContainer.getChildCount(); i++) {
                View childView = mSelectedImagesContainer.getChildAt(i);
                if (childView.getTag().equals(image.mUri)) {
                    mSelectedImagesContainer.removeViewAt(i);
                    break;
                }
            }

            if (mSelectedImages.size() == 0) {
                mSelectedImagesContainer.setVisibility(View.GONE);
                mSelectedImageEmptyMessage.setVisibility(View.VISIBLE);
            }
            return true;
        }
        return false;
    }

    public boolean containsImage(Image image) {
        return mSelectedImages.contains(image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.image_picker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onClick(View view) {
        //cannot use switch statement since ADT 14 -.-
        if (view.getId() == R.id.action_btn_done) {

            Uri[] uris = new Uri[mSelectedImages.size()];
            int i = 0;
            for (Image img : mSelectedImages)
                uris[i++] = img.mUri;

            Intent intent = new Intent();
            intent.putExtra(TAG_IMAGE_URI, uris);
            setResult(Activity.RESULT_OK, intent);
        } else if (view.getId() == R.id.action_btn_cancel) {
            setResult(Activity.RESULT_CANCELED);
        }
        finish();
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new CameraFragment();
                case 1:
                    return new GalleryFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return "Take a photo";//getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return "Gallery";//getString(R.string.title_section2).toUpperCase(l);
            }
            return null;
        }
    }
}
