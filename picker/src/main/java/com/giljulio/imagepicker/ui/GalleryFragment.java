package com.giljulio.imagepicker.ui;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.giljulio.imagepicker.R;
import com.giljulio.imagepicker.model.Image;


/**
 * Created by Gil on 04/03/2014.
 * Galley에서 보여지는 fragment
 */
public class GalleryFragment extends Fragment {

    private static final String TAG = GalleryFragment.class.getSimpleName();

    GridView mGalleryGridView;
    ImageGalleryAdapter mGalleryAdapter;
    ImagePickerActivity mActivity;
    Cursor imageCursor = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        Log.d(TAG, "onCreateView");
        mGalleryAdapter = new ImageGalleryAdapter(getActivity());
        mGalleryGridView = (GridView) rootView.findViewById(R.id.gallery_grid);
        mActivity = ((ImagePickerActivity) getActivity());


//        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID,
//                MediaStore.Images.ImageColumns.ORIENTATION};//1 original example
//        final String[] columns = {MediaStore.Images.Media.DATA,MediaStore.Images.Media.DATE_ADDED,MediaStore.Images.Media._ID,MediaStore.Images.ImageColumns.ORIENTATION};
        final String[] columns = {MediaStore.Images.Media.DATA,MediaStore.Images.Media.DATE_ADDED,MediaStore.Images.Media._ID,MediaStore.Images.ImageColumns.ORIENTATION};
//        final String selection =MediaStore.Images.ImageColumns.DATE_ADDED +"> ?";
//        String before24hour = ((new Date().getTime() - (60 * 60 * 24 * 1000)) / 1000)+"";
//        String[] selectionArgs = {before24hour};2. second example one day

    //    final String orderBy = MediaStore.Images.Media._ID;
        final String orderBy = MediaStore.Images.Media.DATE_ADDED+" DESC";


//        imageCursor=  getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, selection, selectionArgs, orderBy); //2
        imageCursor=  getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
        while (imageCursor.moveToNext()) {


            Uri uri = Uri.parse(imageCursor.getString(imageCursor.getColumnIndex(MediaStore.Images.Media.DATA)));
            int orientation = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.ImageColumns.ORIENTATION));
            mGalleryAdapter.add(new Image(uri, orientation));
        }
        imageCursor.close();


        mGalleryGridView.setAdapter(mGalleryAdapter);
        mGalleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Image image = mGalleryAdapter.getItem(i);
                if (!mActivity.addImage(image)) {
                    mActivity.removeImage(image);
                }

                //refresh the view to
                mGalleryAdapter.getView(i, view, adapterView);
            }
        });

        return rootView;
    }

    class ViewHolder {
        ImageView mThumbnail;
        Image mImage;
    }

    public class ImageGalleryAdapter extends ArrayAdapter<Image> {

        public ImageGalleryAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_gallery_thumbnail, null);
                holder = new ViewHolder();
                holder.mThumbnail = (ImageView) convertView.findViewById(R.id.thumbnail_image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            Image image = getItem(position);
            boolean isSelected = mActivity.containsImage(image);

            ((FrameLayout) convertView).setForeground(isSelected ?
                    getResources().getDrawable(R.drawable.gallery_photo_selected) : null);

            if (holder.mImage == null || !holder.mImage.equals(image)) {
                mActivity.mImageFetcher.loadImage(image.mUri, holder.mThumbnail);
                holder.mImage = image;
            }
            return convertView;
        }
    }

}