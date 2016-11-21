package com.pineco.flickrtron; /**
 * Created by Candace on 11/20/2016.
 */
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder>{

            private ArrayList<FlickrImage> android;
            private Context context;

            public DataAdapter(Context context,ArrayList<FlickrImage> android) {
                this.android = android;
                this.context = context;
            }

            @Override
            public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.feed_layout, viewGroup, false);
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(DataAdapter.ViewHolder viewHolder, int i) {

                viewHolder.caption.setText(android.get(i).getCaption());
                Picasso.with(context).load(android.get(i).geturl()).resize(240, 120).into(viewHolder.img);
            }

            @Override
            public int getItemCount() {
                return android.size();
            }

            public class ViewHolder extends RecyclerView.ViewHolder{
                private TextView caption;
                private ImageView img;
                public ViewHolder(View view) {
                    super(view);

                    caption = (TextView)view.findViewById(R.id.caption);
                    img = (ImageView) view.findViewById(R.id.img);
                }
            }

    }

