package com.gkudva.googleimagesearch;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;

import com.etsy.android.grid.StaggeredGridView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

//import android.widget.SearchView;
//import android.widget.SearchView.OnQueryTextListener;

public class GImageSearch extends AppCompatActivity {

    private ArrayList<GImageModel> gImageModel;
    private GridView gridView;
    private StaggeredGridView staggeredGridView;
    private GImageAdapter adapter;
    private String searchStr;
    private String img_size = null;
    private String color_filter = null;
    private String img_type = null;
    private String search_filter = null;
    private boolean new_search = false;
    private int start_val = 0;
    private final int MAX_VAL = 56; /*Limitation in Google Image search api results*/
    private final int BEGIN_VAL = 0;
    private final int OFFSET = 8; /*Start  offset*/
    private EditText et;
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gimage_search);

        if (isNetworkAvailable()) {
            gImageModel = new ArrayList<GImageModel>();
            adapter = new GImageAdapter(this, gImageModel);
            gridView = (GridView) findViewById(R.id.gridView);
            //staggeredGridView = (StaggeredGridView) findViewById(R.id.staggered_grid_view);
            gridView.setAdapter(adapter);
            //staggeredGridView.setAdapter(adapter);
            gridView.setOnScrollListener(new EndlessScrollListener() {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    if (start_val == MAX_VAL) {
                        start_val = BEGIN_VAL;
                    }
                    start_val += OFFSET;
                    new_search = false;
                    getData();

                }
            });

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    GImageModel item = gImageModel.get(position);
                    Intent sharingIntent = new Intent(getApplicationContext(), GImageSearchFullScreen.class);
                    sharingIntent.putExtra("Item", item);
                    startActivity(sharingIntent);
                }
            });
        }
        else
        {
            Toast.makeText(this, "No Internet connection available", Toast.LENGTH_SHORT).show();
        }
    }

    public String getURL()
    {
        final String img_type_url_arg = "&as_filetype";
        final String color_filter_url_arg= "&imgcolor";
        final String img_size_url_arg = "&imgsz";
        final String search_filter_url_arg = "&as_sitesearch";
        final String equals_url_arg = "=";
        final String default_spinner_arg = "any";
        String gen_url = "";

        if ((img_size != null) && (!img_size.equals(default_spinner_arg)))
        {
            gen_url += img_size_url_arg + equals_url_arg + img_size;
        }

        if ((color_filter != null) && (!color_filter.equals(default_spinner_arg)))
        {
          gen_url += color_filter_url_arg + equals_url_arg + color_filter;
        }

        if ((img_type != null) && (!img_type.equals(default_spinner_arg)))
        {
            gen_url += img_type_url_arg + equals_url_arg + img_type;
        }

        if ((search_filter != null) && (!search_filter.equals(default_spinner_arg)))
        {
            gen_url += search_filter_url_arg + equals_url_arg + search_filter;
        }

        return gen_url;
    }

    public void  getData()
    {
        String url = "https://ajax.googleapis.com/ajax/services/search/images?v=1.0&rsz=8&q=" + searchStr + "&start=" + Integer.toString(start_val)+getURL();

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, null, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, JSONObject response) {

                if (new_search) {
                    adapter.clear();
                }
                adapter.addAll();

                JSONObject respObj = new JSONObject();
                JSONArray respJson = new JSONArray();


                try {
                    respObj = response.getJSONObject("responseData");
                    respJson = respObj.getJSONArray("results");
                    for (int i = 0; i < respJson.length(); i++) {
                        GImageModel ImgModel = new GImageModel();
                        JSONObject foto = (JSONObject) respJson.get(i);
                        ImgModel.imgUrl = foto.getString("url");
                        ImgModel.text = foto.getString("title");

                        gImageModel.add(ImgModel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                adapter.notifyDataSetChanged();
                //adapter.notifyDataSetInvalidated();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                //super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gimage_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // perform query here
                searchStr = query;
                new_search = true;
                getData();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            getFilterValues();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getFilterValues()
    {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = (int) (metrics.widthPixels * 1);
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_layout);
        dialog.setTitle("Advanced Filters");
        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(screenWidth, screenWidth);



        Button btnSave = (Button) dialog.findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_size = getSpinnerValue(R.id.spImgSizeVal);
                color_filter = getSpinnerValue(R.id.spColorFilterVal);
                img_type = getSpinnerValue(R.id.spImageTypeVal);
                search_filter = getEditTextVal(R.id.etSiteFilterVal);
                dialog.dismiss();
            }
        });

        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_size = null;
                color_filter = null;
                img_type = null;
                search_filter = null;
                dialog.cancel();
            }
        });


    }

    public String getSpinnerValue(int spinner_id)
    {
        Spinner mySpinner=(Spinner) dialog.findViewById(spinner_id);
        return mySpinner.getSelectedItem().toString();
    }

    public String getEditTextVal(int et_id)
    {
        EditText edText = (EditText) dialog.findViewById(et_id);
        if (edText.getText().toString().matches(""))
        {
            /*No input*/
            return null;
        }
        return edText.getText().toString();
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }
}
