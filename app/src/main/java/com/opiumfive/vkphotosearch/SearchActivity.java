package com.opiumfive.vkphotosearch;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKPhotoArray;

public class SearchActivity extends AppCompatActivity {

    private static final String DIALOG_IMAGE = "image";
    private VKRequest myRequest;
    FloatingActionButton floatingActionButton;
    GridView gridView;
    VKPhotoArray photoArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        gridView = (GridView) findViewById(R.id.gridView);
        setupAdapter();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                startActivityForResult(i,1);
            }
        });
    }


    void setupAdapter() {
        if ( gridView == null) return;
        if (photoArray != null) {
            gridView.setAdapter(new ArrayAdapter<VKApiPhoto>(this,android.R.layout.simple_gallery_item, photoArray));
            gridView.setAdapter(new GalleryItemAdapter(photoArray));
        } else {
            gridView.setAdapter(null);
        }
    }

    private class GalleryItemAdapter extends ArrayAdapter<VKApiPhoto> {
        public GalleryItemAdapter(VKPhotoArray items) {
            super(getApplicationContext(),0,items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.gallery_item, parent, false);
            }
            ImageView imageView = (ImageView)convertView
                    .findViewById(R.id.gallery_item_ImageView);
            String url = getItem(position).toString();
            final String url_big = getBiggestNotEmptyImage(getItem(position));

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fm = getSupportFragmentManager();
                    ImageFragment.newInstance(url_big).show( fm, DIALOG_IMAGE);
                }
            });

            Picasso.with(getApplicationContext()).load(url).into(imageView);
            return convertView;
        }
    }

    public String getBiggestNotEmptyImage(VKApiPhoto photo) {
        if (!photo.photo_2560.isEmpty())
            return photo.photo_2560;
        if (!photo.photo_1280.isEmpty())
            return photo.photo_1280;
        if (!photo.photo_807.isEmpty())
            return photo.photo_807;
        if (!photo.photo_604.isEmpty())
            return photo.photo_604;
        return "http://abitur.psuti.ru/uploads/spiski/002bi_files/red-question.png";
    }

    VKRequest.VKRequestListener mRequestListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            photoArray = (VKPhotoArray) response.parsedModel;
            setupAdapter();
        }

        @Override
        public void onError(VKError error) {
        }

        @Override
        public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded,
                               long bytesTotal) {
        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.search) {
            onSearchRequested();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Здесь будет храниться то, что пользователь ввёл в поисковой строке
            String search = intent.getStringExtra(SearchManager.QUERY);
            VKRequest request = VKRequest.getRegisteredRequest(VKApi.photos().search(search,0,0,0,0, 100).registerObject());
            myRequest = request;
            myRequest.executeWithListener(mRequestListener);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        Double lat = data.getDoubleExtra("lat", 55.75);
        Double longi = data.getDoubleExtra("long", 37.61);

        VKRequest request = VKRequest.getRegisteredRequest(VKApi.photos().search("",lat,longi,0,0, 100).registerObject());
        myRequest = request;
        myRequest.executeWithListener(mRequestListener);
    }
}
