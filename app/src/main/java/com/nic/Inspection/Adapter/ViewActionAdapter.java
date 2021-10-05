package com.nic.Inspection.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hanks.htextview.rainbow.RainbowTextView;
import com.nic.Inspection.Activity.ViewActionImageScreen;
import com.nic.Inspection.Model.BlockListValue;
import com.nic.Inspection.R;
import com.nic.Inspection.Support.MyCustomTextView;
import com.nic.Inspection.constant.AppConstant;
import com.nic.Inspection.session.PrefManager;

import java.util.List;

public class ViewActionAdapter extends RecyclerView.Adapter<ViewActionAdapter.MyViewHolder> {

    private PrefManager prefManager;
    private Context context;
    private List<BlockListValue> actionListValues;

    public ViewActionAdapter(Context context, List<BlockListValue> actionListValues) {

        this.context = context;
        prefManager = new PrefManager(context);
        this.actionListValues = actionListValues;
    }

    @Override
    public ViewActionAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_actions, parent, false);
        return new ViewActionAdapter.MyViewHolder(itemView);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public MyCustomTextView date_of_action,action_remark,action_result_tv,action_on_off,action_taken_tv,action_desigantion_tv;
        public RainbowTextView rainbowTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            date_of_action = (MyCustomTextView) itemView.findViewById(R.id.date_of_action);
            action_taken_tv = (MyCustomTextView) itemView.findViewById(R.id.action_taken_tv);
            action_desigantion_tv = (MyCustomTextView) itemView.findViewById(R.id.action_desigantion_tv);
            rainbowTextView = (RainbowTextView) itemView.findViewById(R.id.view_action_image);
            action_remark = (MyCustomTextView) itemView.findViewById(R.id.action_remark);
            action_result_tv = (MyCustomTextView) itemView.findViewById(R.id.action_result_tv);
            action_on_off = (MyCustomTextView) itemView.findViewById(R.id.action_on_off);

            rainbowTextView.animateText(context.getString(R.string.view_action_image));

        }


        @Override
        public void onClick(View v) {

        }
    }

    public void actionImagePreviewScreen(int position) {
        String inspect_id = String.valueOf(actionListValues.get(position).getInspectionID());
        String action_id = String.valueOf(actionListValues.get(position).getActionID());
        String delete_flag = String.valueOf(actionListValues.get(position).getDelete_Flag());

        Activity activity = (Activity) context;
        Intent intent = new Intent(context, ViewActionImageScreen.class);
        intent.putExtra(AppConstant.INSPECTION_ID,inspect_id);
        intent.putExtra(AppConstant.ACTION_ID,action_id);
        intent.putExtra(AppConstant.DELETE_FLAG,delete_flag);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Override
    public void onBindViewHolder(final ViewActionAdapter.MyViewHolder holder, final int position) {
        holder.date_of_action.setText(actionListValues.get(position).getDate_of_Action());
        holder.action_remark.setText(actionListValues.get(position).getAction_remark());
        holder.action_desigantion_tv.setText(actionListValues.get(position).getActionOffDesignName());
        holder.action_taken_tv.setText(actionListValues.get(position).getActionOffName());
        holder.action_result_tv.setText(actionListValues.get(position).getActionresult());
        holder.action_on_off.setText(actionListValues.get(position).getDelete_Flag());
        if(prefManager.getLevels().equalsIgnoreCase("B")){
            holder.action_on_off.setVisibility(View.VISIBLE);
        }
        else {
            holder.action_on_off.setVisibility(View.GONE);
        }

        holder.rainbowTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionImagePreviewScreen(position);

            }
        });

    }

    @Override
    public int getItemCount() {
        return actionListValues.size();
    }

}
