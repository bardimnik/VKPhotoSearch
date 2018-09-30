package com.opiumfive.vkphotosearch;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;


public class ImageFragment extends DialogFragment implements View.OnClickListener {

    private String path;
    private String uid;

    public static ImageFragment newInstance(String imagePath, String userID) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putSerializable("image_path", imagePath);
        args.putSerializable("user_id", userID);
        fragment.setArguments(args);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ImageView imageView = new ImageView(getActivity());
        WindowManager w = getActivity().getWindowManager();
        Point size = new Point();
        w.getDefaultDisplay().getSize(size);
        imageView.setMinimumWidth(size.x);
        imageView.setBackgroundColor(Color.BLACK);
        imageView.setOnClickListener(this);
        path = (String)getArguments().getSerializable("image_path");
        uid = (String)getArguments().getSerializable("user_id");
        Picasso.get().load(path).into(imageView);
        return imageView;
    }

    @Override
    public void onClick(View view) {
        if (uid != null && !uid.isEmpty()) {
            if (uid.contains("-")) {
                uid = uid.replace("id-", "club");
            }
            String link = "https://vk.com/" + uid;
            Toast.makeText(getActivity(), link, Toast.LENGTH_SHORT).show();
            Uri address = Uri.parse(link);
            Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
            startActivity(openlinkIntent);
        }
    }
}
