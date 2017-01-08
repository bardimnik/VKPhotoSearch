package com.opiumfive.vkphotosearch;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
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


public class ImageFragment extends DialogFragment implements View.OnLongClickListener, View.OnClickListener {
    private ImageView imageView;
    String path;
    String uid;
    DownloadManager dm;
    private  long enqueue;

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
        imageView = new ImageView(getActivity());

        WindowManager w = getActivity().getWindowManager();
        Point size = new Point();
        w.getDefaultDisplay().getSize(size);
        imageView.setMinimumWidth(size.x);
        imageView.setBackgroundColor(Color.BLACK);

        imageView.setOnLongClickListener(this);
        imageView.setOnClickListener(this);
        path = (String)getArguments().getSerializable("image_path");
        uid = (String)getArguments().getSerializable("user_id");
        Picasso.with(getActivity()).load(path).into(imageView);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = dm.query(query);
                    if (c.moveToFirst()) {
                        int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                        }
                    }
                }
            }
        };

        getActivity().registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        return imageView;
    }

    @Override
    public boolean onLongClick(View view) {
        //dm = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
        //DownloadManager.Request request = new DownloadManager.Request(
        //        Uri.parse(path));
        //enqueue = dm.enqueue(request);
        return false;
    }

    @Override
    public void onClick(View view) {
        if (uid != null && !uid.isEmpty()) {
            String link = "https://vk.com/" + uid;
            Toast.makeText(getActivity(), link, Toast.LENGTH_SHORT).show();
            Uri address = Uri.parse(link);
            Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
            startActivity(openlinkIntent);
        }
    }
}
