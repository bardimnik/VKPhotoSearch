package com.opiumfive.vkphotosearch;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by allsw on 11.07.2016.
 */

public class ImageFragment extends DialogFragment {
    private ImageView imageView;

    public static ImageFragment newInstance(String imagePath) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putSerializable("image_path", imagePath);
        fragment.setArguments(args);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        imageView = new ImageView(getActivity());
        String path = (String)getArguments().getSerializable("image_path");
        Picasso.with(getActivity()).load(path).into(imageView);
        return imageView;

    }
}
