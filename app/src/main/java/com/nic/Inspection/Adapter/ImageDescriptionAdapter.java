package com.nic.Inspection.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nic.Inspection.Activity.FullImageActivity;
import com.nic.Inspection.Model.BlockListValue;
import com.nic.Inspection.R;
import com.nic.Inspection.Support.MyCustomTextView;
import com.nic.Inspection.constant.AppConstant;
import com.nic.Inspection.session.PrefManager;

import java.util.List;

/**
 * Created by NIC on 13-02-2019.
 */

public class ImageDescriptionAdapter extends RecyclerView.Adapter<ImageDescriptionAdapter.MyViewHolder> implements View.OnClickListener {

    private Context context;
    private PrefManager prefManager;
    private List<BlockListValue> imagelistvalues;

    public ImageDescriptionAdapter(Context context, List<BlockListValue> imagelistvalues) {

        this.context = context;
        prefManager = new PrefManager(context);
        this.imagelistvalues = imagelistvalues;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.save_image_with_description, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onClick(View v) {

    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView preview_image_view;
        private MyCustomTextView description;

        public MyViewHolder(View itemView) {
            super(itemView);
            preview_image_view = (ImageView) itemView.findViewById(R.id.preview_image_view);
            description = (MyCustomTextView) itemView.findViewById(R.id.description);
        }

    }

    public void FullImageAcivity(int position) {

        Activity activity = (Activity) context;
        Intent intent = new Intent(context, FullImageActivity.class);
        intent.putExtra(AppConstant.INSPECTION_ID, prefManager.getAppKey());
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.description.setText(imagelistvalues.get(position).getDescription());
        holder.preview_image_view.setImageBitmap(imagelistvalues.get(position).getImage());
        holder.preview_image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullImageAcivity(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagelistvalues.size();
    }
}
