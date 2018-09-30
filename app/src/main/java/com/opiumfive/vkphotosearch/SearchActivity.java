package com.opiumfive.vkphotosearch;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
    private GridView gridView;
    private VKPhotoArray photoArray;  // массив объектов фото
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setEmptyView(findViewById(R.id.emptyView));
        findViewById(R.id.rate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str ="https://play.google.com/store/apps/details?id=com.opiumfive.vkphotosearch";
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(str)));
            }
        });
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        if (savedInstanceState != null) {
            photoArray = savedInstanceState.getParcelable("array");  // при повороте экрана-сворачивании восстановление состояния
        }
        setupAdapter();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab); // кнопка для открытия карт

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MapsActivity.class);
                startActivityForResult(i,1);
            }
        });

    }

    void setupAdapter() {
        if ( gridView == null) return;
        if (photoArray != null) {
            gridView.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_gallery_item, photoArray));
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
            ImageView imageView = (ImageView)convertView.findViewById(R.id.gallery_item_ImageView);
            String url = getItem(position).toString();
            final String url_big = getBiggestNotEmptyImage(getItem(position));
            final String uid = "id" + getItem(position).owner_id;
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    FragmentManager fm = getSupportFragmentManager();
                    ImageFragment.newInstance(url_big, uid).show( fm, DIALOG_IMAGE);
                }
            });

            Picasso.get().load(url).into(imageView); //  загрузка фотографий в грид
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
        return "";
    }

    VKRequest.VKRequestListener mRequestListener = new VKRequest.VKRequestListener() {
        @Override
        public void onComplete(VKResponse response) {
            photoArray = (VKPhotoArray) response.parsedModel; // полученные фото в gson
            progressBar.setVisibility(View.INVISIBLE);
            setupAdapter();
            if (photoArray.isEmpty())
                Toast.makeText(getApplicationContext(), R.string.nothing,Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(VKError error) {
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onProgress(VKRequest.VKProgressType progressType, long bytesLoaded, long bytesTotal) {
        }

        @Override
        public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.length() >= 1) getPhotos(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(true);
        return super.onCreateOptionsMenu(menu);
    }

    private void getPhotos(String query) {
        VKRequest request = VKRequest.getRegisteredRequest(VKApi.photos().search(query,0,0,0,0, 300, 5000).registerObject());
        myRequest = request;
        myRequest.executeWithListener(mRequestListener);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.search) {
            onSearchRequested();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        Double lat = data.getDoubleExtra("lat", 55.75); // получаем координаты и выполняем запрос фото
        Double longi = data.getDoubleExtra("long", 37.61);
        int radius = data.getIntExtra("rad", 5000);
        SPmanager prefs = SPmanager.getInstance();
        prefs.setLastLat(lat.floatValue());
        prefs.setLastLong(longi.floatValue());
        VKRequest request = VKRequest.getRegisteredRequest(VKApi.photos().search("",lat,longi,0,0, 500, radius).registerObject());
        myRequest = request;
        myRequest.executeWithListener(mRequestListener);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myRequest != null)
            myRequest.cancel();
    }
}
