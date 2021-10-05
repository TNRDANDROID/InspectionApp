package com.nic.Inspection.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nic.Inspection.Model.BlockListValue;
import com.nic.Inspection.R;
import com.nic.Inspection.Support.MyCustomTextView;
import com.nic.Inspection.session.PrefManager;

import java.util.List;

/**
 * Created by NIC on 21-02-2019.
 */

public class ViewActionImageAdapter extends RecyclerView.Adapter<ViewActionImageAdapter.MyViewHolder> {
    private PrefManager prefManager;
    private Context context;

    private List<BlockListValue> actionImageList;

    public ViewActionImageAdapter(Context context, List<BlockListValue> actionImageList) {

        this.context = context;
        prefManager = new PrefManager(context);
        this.actionImageList = actionImageList;
    }

    @Override
    public ViewActionImageAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_action_image, parent, false);
        return new MyViewHolder(itemView);
    }





    public class MyViewHolder extends RecyclerView.ViewHolder {
        private ImageView preview_action_image_view;
        private MyCustomTextView action_description;

        public MyViewHolder(View itemView) {
            super(itemView);
            preview_action_image_view = (ImageView) itemView.findViewById(R.id.preview_action_image_view);
            action_description = (MyCustomTextView) itemView.findViewById(R.id.action_description);
        }
    }
    @Override
    public void onBindViewHolder(ViewActionImageAdapter.MyViewHolder holder, final int position) {
        holder.action_description.setText(actionImageList.get(position).getDescription());
        holder.preview_action_image_view.setImageBitmap(actionImageList.get(position).getImage());
    }

    @Override
    public int getItemCount() {
        return actionImageList.size();
    }
}
