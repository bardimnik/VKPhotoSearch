package com.opiumfive.vkphotosearch;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
    ProgressBar progressBar;


    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(getApplicationContext(),"Введите поисковый запрос или выберите точку на карте.", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setEmptyView(findViewById(R.id.textView));
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        if (savedInstanceState != null)
            photoArray = savedInstanceState.getParcelable("array");  // при повороте экрана-сворачивании восстановление состояния
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
            ImageView imageView = (ImageView)convertView
                    .findViewById(R.id.gallery_item_ImageView);
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

            Picasso.with(getApplicationContext()).load(url).into(imageView); //  загрузка фотографий в грид
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
            photoArray = (VKPhotoArray) response.parsedModel; // полученные фото в gson
            progressBar.setVisibility(View.INVISIBLE);
            setupAdapter();
            if (photoArray.isEmpty())
                Toast.makeText(getApplicationContext(),"Ничего не найдено.",Toast.LENGTH_SHORT).show();

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
        int id = item.getItemId();

        if (id == R.id.search) {
            onSearchRequested();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
            // Здесь будет храниться то, что пользователь ввёл в поисковой строке
            String search = intent.getStringExtra(SearchManager.QUERY); // поисковой запрос
            VKRequest request = VKRequest.getRegisteredRequest(VKApi.photos().search(search,0,0,0,0, 300).registerObject());
            myRequest = request;
            myRequest.executeWithListener(mRequestListener);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {return;}
        Double lat = data.getDoubleExtra("lat", 55.75); // получаем координаты и выполняем запрос фото
        Double longi = data.getDoubleExtra("long", 37.61);
        VKRequest request = VKRequest.getRegisteredRequest(VKApi.photos().search("",lat,longi,0,0, 500).registerObject());
        myRequest = request;
        myRequest.executeWithListener(mRequestListener);
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(getApplicationContext(),"Координаты выбраны, сейчас найдем фото.",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myRequest != null)
            myRequest.cancel();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("array",photoArray);
        if (myRequest != null) {
            outState.putLong("request", myRequest.registerObject());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        long requestId = savedInstanceState.getLong("request");
        myRequest = VKRequest.getRegisteredRequest(requestId);
        if (myRequest != null) {
            myRequest.unregisterObject();
            myRequest.setRequestListener(mRequestListener);
        }
    }
}
