package com.gkudva.googleimagesearch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class GImageSearchFullScreen extends AppCompatActivity {
    private TextView tv;
    private ImageView iv;
    private GImageModel item;
    private ShareActionProvider miShareAction;
    private TouchImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gimage_search_full_screen);

        Bundle extras = getIntent().getExtras();
        if (null == extras)
        {
            return;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tv = (TextView) findViewById(R.id.tvFullScreen);
        iv = (ImageView) findViewById(R.id.ivFullScreen);
        image = (TouchImageView) findViewById(R.id.touchiv);

        item = (GImageModel)extras.getSerializable("Item") ;
        tv.setText(Html.fromHtml(item.text));

        Picasso.with(getApplicationContext()).load(item.imgUrl).into(image, new Callback() {
            @Override
            public void onSuccess() {
                setupShareIntent();

            }

            @Override
            public void onError() {

            }
        });

        image.setOnTouchImageViewListener(new TouchImageView.OnTouchImageViewListener() {
            @Override
            public void onMove() {
                //Toast.makeText(getApplicationContext(), "setOnTouchImageViewListener", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gimage_search_full_screen, menu);
        MenuItem item = menu.findItem(R.id.action_share);
        miShareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setupShareIntent()
    {
        try {
            // Fetch Bitmap Uri locally
            ImageView ivImage = (ImageView) findViewById(R.id.ivFullScreen);
            Uri bmpUri = getLocalBitmapUri(ivImage); // see previous remote images section
            // Create share intent as described above
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
            // Attach share event to the menu item provider
            miShareAction.setShareIntent(shareIntent);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp = null;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            File file =  new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
            file.getParentFile().mkdirs();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }
}
