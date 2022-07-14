package com.nic.Inspection.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.nic.Inspection.Activity.AddInspectionReportScreen;
import com.nic.Inspection.Activity.LoginScreen;
import com.nic.Inspection.Activity.ViewInspectionReportScreen;
import com.nic.Inspection.DataBase.DBHelper;
import com.nic.Inspection.Model.BlockListValue;
import com.nic.Inspection.R;
import com.nic.Inspection.Support.MyCustomTextView;
import com.nic.Inspection.Utils.Utils;
import com.nic.Inspection.constant.AppConstant;
import com.nic.Inspection.session.PrefManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by AchanthiSundar on 04-01-2019.
 */

public class ProjectListAdapter extends RecyclerView.Adapter<ProjectListAdapter.MyViewHolder> implements Filterable {
    private Context context;
    private List<BlockListValue> projectListValues;
    private List<BlockListValue> projectListFiltered;
    private ProjectsAdapterListener listener;
    private PrefManager prefManager;

    public ProjectListAdapter(Context context, List<BlockListValue> projectListValues, ProjectsAdapterListener listener) {
        this.context = context;
        this.listener = listener;
        this.projectListValues = projectListValues;
        this.projectListFiltered = projectListValues;
        prefManager = new PrefManager(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.project_list, parent, false);
        return new MyViewHolder(itemView);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MyCustomTextView projectName, amountTv, levelTv, viewReport, addReport;

        public MyViewHolder(View itemView) {
            super(itemView);
            projectName = (MyCustomTextView) itemView.findViewById(R.id.project_title_tv);
            amountTv = (MyCustomTextView) itemView.findViewById(R.id.amount_tv);
            levelTv = (MyCustomTextView) itemView.findViewById(R.id.level_tv);
            viewReport = (MyCustomTextView) itemView.findViewById(R.id.view_inspection_report);
            addReport = (MyCustomTextView) itemView.findViewById(R.id.add_inspection_report);
            if(prefManager.getLevels().equalsIgnoreCase("B")){
                addReport.setVisibility(View.GONE);
            }
        }


        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.view_inspection_report:
                    // viewInspectionReport();
                    break;
                case R.id.add_inspection_report:
                    // addInspectionReport();

            }


        }

    }


    public void viewInspectionReport(int position) {

        String workid = projectListFiltered.get(position).getWorkID();
        String workName = projectListFiltered.get(position).getWorkName();
        String workGroupID = projectListFiltered.get(position).getWorkGroupID();
        String workTypeID = projectListFiltered.get(position).getWorkTypeID();
        String stageName = projectListFiltered.get(position).getWorkStageName();
        String asAmount = projectListFiltered.get(position).getAsAmount();
        Log.d("viewworkId",""+workid);
        prefManager.setKeyActionProjectName(workName);
        prefManager.setKeyActionAmount(asAmount);
        prefManager.setKeyActionWorkid(workid);
        prefManager.setKeyActionStageLevel(stageName);

        Activity activity = (Activity) context;
        Intent intent = new Intent(context, ViewInspectionReportScreen.class);

        intent.putExtra(AppConstant.WORK_ID,workid);
        intent.putExtra(AppConstant.WORK_NAME, workName);
        intent.putExtra(AppConstant.WORK_GROUP_ID, workGroupID);
        intent.putExtra(AppConstant.WORK_TYPE_ID, workTypeID);
        intent.putExtra(AppConstant.WORK_SATGE_NAME, stageName);
        intent.putExtra(AppConstant.AS_AMOUNT, asAmount);

        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    public void addInspectionReport(int position) {

        String workid = projectListFiltered.get(position).getWorkID();
        String workName = projectListFiltered.get(position).getWorkName();
        String workGroupID = projectListFiltered.get(position).getWorkGroupID();
        String workTypeID = projectListFiltered.get(position).getWorkTypeID();
        String stageName = projectListFiltered.get(position).getWorkStageName();
        String stageCode = projectListFiltered.get(position).getWorkStageCode();
        String asAmount = projectListFiltered.get(position).getAsAmount();
                                                    /* to check inspection to be only once per day */
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date_of_inspection = sdf.format(new Date());

        int count = 0;

        String sql = "select * from " + DBHelper.INSPECTION+ " where work_id = '"+workid+ "' and date_of_inspection = '"+date_of_inspection+"'";
        Cursor stages = getRawEvents(sql, null);
        Log.d("date_sql", sql);
        if(stages.getCount() > 0){
           count = stages.getCount();
        }
        else {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
            String date_of_inspection1 = sdf1.format(new Date());

            String sql1 = "select * from " + DBHelper.INSPECTION_PENDING+ " where work_id = '"+workid+ "' and date_of_inspection = '"+date_of_inspection1+"'";
            Cursor stages1 = getRawEvents(sql1, null);
            if(stages1.getCount() > 0){
                count = stages1.getCount();
            }

        }
        if(count > 0){
            Log.d("already_there","found");
            Utils.showAlert(this.context,"Already Inspected");
        }
        else {
            Activity activity = (Activity) context;
            Intent intent = new Intent(context, AddInspectionReportScreen.class);

            intent.putExtra(AppConstant.WORK_ID, workid);
            intent.putExtra(AppConstant.WORK_NAME, workName);
            intent.putExtra(AppConstant.WORK_GROUP_ID, workGroupID);
            intent.putExtra(AppConstant.WORK_TYPE_ID, workTypeID);
            intent.putExtra(AppConstant.WORK_SATGE_NAME, stageName);
            intent.putExtra(AppConstant.WORK_STAGE_CODE, stageCode);
            intent.putExtra(AppConstant.AS_AMOUNT, asAmount);

            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
            activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        }
    }


    @Override
    public void onBindViewHolder(final ProjectListAdapter.MyViewHolder holder, final int position) {
//        final BlockListValue blockListValue = projectListFiltered.get(position);
        holder.projectName.setText(projectListFiltered.get(position).getWorkName());
        holder.amountTv.setText(projectListFiltered.get(position).getAsAmount());
        holder.levelTv.setText(projectListFiltered.get(position).getWorkStageName());
        if (!prefManager.getLevels().equalsIgnoreCase("B")) {
            holder.addReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addInspectionReport(position);
                }
            });
        }

        holder.viewReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewInspectionReport(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return projectListFiltered.size();
    }

    public void loadDBValues() {

    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    projectListFiltered = projectListValues;
                } else {
                    List<BlockListValue> filteredList = new ArrayList<>();
                    for (BlockListValue row : projectListValues) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getWorkName().toLowerCase().contains(charString.toLowerCase()) || row.getWorkName().contains(charSequence)) {
                            filteredList.add(row);
                        }
                    }

                    projectListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = projectListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                projectListFiltered = (ArrayList<BlockListValue>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public interface ProjectsAdapterListener {
        // void setProjectList(BlockListValue projectList);
        void addInspectionOnclick(View v, int position);
    }

    public Cursor getRawEvents(String sql, String string) {
        Cursor cursor = LoginScreen.db.rawQuery(sql, null);
        return cursor;
    }
}
