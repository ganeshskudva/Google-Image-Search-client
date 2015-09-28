package com.gkudva.googleimagesearch;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by gkudva on 9/19/15.
 */
public class GImageAdapter extends ArrayAdapter {
    public GImageAdapter(Context context, List<GImageModel> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        GImageModel imgItem = (GImageModel)getItem(position);

        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item_layout, parent, false);
        }

        ImageView img = (ImageView) convertView.findViewById(R.id.image);
        Picasso.with(getContext()).load(imgItem.imgUrl).memoryPolicy(MemoryPolicy.NO_CACHE).networkPolicy(NetworkPolicy.NO_CACHE).into(img);
        TextView imgText= (TextView) convertView.findViewById(R.id.tvText);
        imgText.setText(Html.fromHtml(imgItem.text));

        return convertView;
    }
}
