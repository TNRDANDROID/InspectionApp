package com.nic.Inspection.Adapter;

import android.app.Activity;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.nic.Inspection.Activity.Dashboard;
import com.nic.Inspection.DataBase.DBHelper;
import com.nic.Inspection.Fragment.PendingLayoutFragment;
import com.nic.Inspection.Model.BlockListValue;
import com.nic.Inspection.R;
import com.nic.Inspection.Support.MyCustomTextView;
import com.nic.Inspection.Utils.Utils;
import com.nic.Inspection.constant.AppConstant;
import com.nic.Inspection.session.PrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.nic.Inspection.Activity.LoginScreen.db;

/**
 * Created by NIC on 21-02-2019.
 */

public class PendingLayoutAdapter extends RecyclerView.Adapter<PendingLayoutAdapter.MyViewHolder> {

    private static Activity context;
    private List<BlockListValue> pendingListValues;
    static PrefManager prefManager;
    static JSONObject dataset = new JSONObject();
    private PendingLayoutFragment pendingLayoutFragment;

    public PendingLayoutAdapter(Activity context, List<BlockListValue> pendingListValues, PendingLayoutFragment pendingLayoutFragment) {

        this.context = context;
        prefManager = new PrefManager(context);
        this.pendingListValues = pendingListValues;
        this.pendingLayoutFragment = pendingLayoutFragment;
    }

    @Override
    public PendingLayoutAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pending_layout, parent, false);
        return new MyViewHolder(itemView);


    }

    @Override
    public void onBindViewHolder(PendingLayoutAdapter.MyViewHolder holder, final int position) {

        holder.pend_work_id.setText(pendingListValues.get(position).getWorkID());
        if(prefManager.getLevels().equalsIgnoreCase("D") || prefManager.getLevels().equalsIgnoreCase("S")){
            holder.pend_stage.setText(pendingListValues.get(position).getWorkStageName());
        }

        if (prefManager.getLevels().equalsIgnoreCase("D") || prefManager.getLevels().equalsIgnoreCase("S")) {
            holder.pend_inspected_date.setText(Utils.formatDate(pendingListValues.get(position).getDate_of_inspection()));
        }else if(prefManager.getLevels().equalsIgnoreCase("B")){
            holder.pend_inspected_date.setText(Utils.formatDate(pendingListValues.get(position).getDate_of_Action()));
        }

        if (prefManager.getLevels().equalsIgnoreCase("D") || prefManager.getLevels().equalsIgnoreCase("S")) {
            holder.pend_observation.setText(pendingListValues.get(position).getObservation());
        }else if(prefManager.getLevels().equalsIgnoreCase("B")){
            holder.pend_observation.setText(pendingListValues.get(position).getAction_remark());
        }




        if (prefManager.getLevels().equalsIgnoreCase("B")) {
          //  holder.date.setText("Action Date");
        }

        holder.add_inspection_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (prefManager.getLevels().equalsIgnoreCase("D") || prefManager.getLevels().equalsIgnoreCase("S")) {
                   UploadPending_Inspection(position);
                }else
                if (prefManager.getLevels().equalsIgnoreCase("B")) {
                    UploadPending_Action(position);
                }

            }
        });

        holder.del_inspection_report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prefManager.getLevels().equalsIgnoreCase("D") || prefManager.getLevels().equalsIgnoreCase("S")) {
                    deletePending_Inspection(position);
                }else
                if (prefManager.getLevels().equalsIgnoreCase("B")) {
                    deletePending_Action(position);

                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return pendingListValues.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private MyCustomTextView pend_work_id, pend_stage, pend_inspected_date, pend_observation, add_inspection_report, del_inspection_report,date,remark_Of_remark;
        private LinearLayout stageLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            pend_work_id = (MyCustomTextView) itemView.findViewById(R.id.pend_work_id);
            pend_stage = (MyCustomTextView) itemView.findViewById(R.id.pend_stage);
            pend_inspected_date = (MyCustomTextView) itemView.findViewById(R.id.pend_inspected_date);
            pend_observation = (MyCustomTextView) itemView.findViewById(R.id.pend_observation);
            add_inspection_report = (MyCustomTextView) itemView.findViewById(R.id.add_inspection_report);
            del_inspection_report = (MyCustomTextView) itemView.findViewById(R.id.del_inspection_report);
            remark_Of_remark = (MyCustomTextView) itemView.findViewById(R.id.remark_Of_remark);
            date = (MyCustomTextView) itemView.findViewById(R.id.date);
            stageLayout = (LinearLayout) itemView.findViewById(R.id.stageLayout);

            if (prefManager.getLevels().equalsIgnoreCase("B")) {
                stageLayout.setVisibility(View.GONE);
                remark_Of_remark.setText("Remark");
                date.setText("Action Date");
            }
        }
    }

    public void UploadPending_Inspection(int position) {

        String work_id = pendingListValues.get(position).getWorkID();
        int inspection_id = pendingListValues.get(position).getInspectionID();
        prefManager.setKeyDeleteId(String.valueOf(inspection_id));
        String stage_of_work_on_inspection = pendingListValues.get(position).getWorkStageCode();
        String stage_of_work_on_inspection_name = pendingListValues.get(position).getWorkStageName();
        String date_of_inspection = pendingListValues.get(position).getDate_of_inspection();
        int observation = pendingListValues.get(position).getObservationID();
        String inspection_remark = pendingListValues.get(position).getInspection_remark();
        String created_date = pendingListValues.get(position).getCreatedDate();
        String created_ipaddress = pendingListValues.get(position).getCreatedIpAddress();
        String created_username = pendingListValues.get(position).getCreatedUserName();
        String Observation = pendingListValues.get(position).getObservation();
        String ae_username = pendingListValues.get(position).getAEUserName();
        String dcode = null;
        if(prefManager.getLevels().equalsIgnoreCase("S")) {
            dcode = pendingListValues.get(position).getDistictCode();
        }


        try {
            dataset.put(AppConstant.KEY_SERVICE_ID, AppConstant.KEY_HIGH_VALUE_PROJECT_INSPECTION_SAVE);
            dataset.put(AppConstant.WORK_ID, work_id);
            dataset.put(AppConstant.STAGE_OF_WORK_ON_INSPECTION, stage_of_work_on_inspection);
            dataset.put(AppConstant.AE_USERNAME, ae_username);
            dataset.put(AppConstant.DATE_OF_INSPECTION, date_of_inspection);
            dataset.put(AppConstant.OBSERVATION, observation);
            dataset.put(AppConstant.INSPECTION_REMARK, inspection_remark);
            dataset.put(AppConstant.CREATED_DATE, created_date);
            dataset.put(AppConstant.CREATED_IMEI_NO, created_ipaddress);
            dataset.put(AppConstant.CREATED_USER_NAME, created_username);
            if(prefManager.getLevels().equalsIgnoreCase("S")) {
                dataset.put(AppConstant.DISTRICT_CODE, dcode);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray imageJson = new JSONArray();

        String image_sql = "Select * from " + DBHelper.CAPTURED_PHOTO + " where inspection_id =" + inspection_id + " and work_id =" + work_id;
        Log.d("sql", image_sql);
        Cursor image = getRawEvents(image_sql, null);

        if (image.getCount() > 0) {
            int i = 0;
            if (image.moveToFirst()) {
                do {
                    String image_work_id = image.getString(image.getColumnIndexOrThrow(AppConstant.WORK_ID));
                    int image_inspection_id = image.getInt(image.getColumnIndexOrThrow(AppConstant.INSPECTION_ID));
                    String latitude = image.getString(image.getColumnIndexOrThrow(AppConstant.LATITUDE));
                    String longitude = image.getString(image.getColumnIndexOrThrow(AppConstant.LONGITUDE));
                    String images = image.getString(image.getColumnIndexOrThrow(AppConstant.IMAGE));
                    String description = image.getString(image.getColumnIndexOrThrow(AppConstant.DESCRIPTION));

                    JSONArray imageArray = new JSONArray();

                    imageArray.put(i);
                    imageArray.put(image_work_id);
                    imageArray.put(latitude);
                    imageArray.put(longitude);
                    imageArray.put(images.trim());
                    imageArray.put(description);
                    i++;
                    imageJson.put(imageArray);

                } while (image.moveToNext());
            }
        }

        try {
            dataset.put("image_details", imageJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String oof = dataset.toString();
        int maxLogSize = 1000;
        for (int i = 0; i <= oof.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > oof.length() ? oof.length() : end;
            Log.v("oof", oof.substring(start, end));
        }
        if (Utils.isOnline()) {
            pendingLayoutFragment.pending_Sync_Data(dataset);

        } else {
            Utils.showAlert(context, "Turn On Mobile Data To Upload");
        }


    }

    public void UploadPending_Action(int position) {

        String work_id = pendingListValues.get(position).getWorkID();
        int inspection_id = pendingListValues.get(position).getInspectionID();
        int action_id = pendingListValues.get(position).getActionID();
        prefManager.setKeyDeleteId(String.valueOf(action_id));
        String date_of_action = pendingListValues.get(position).getDate_of_Action();
        String action_remark = pendingListValues.get(position).getAction_remark();



        try {
            dataset.put(AppConstant.KEY_SERVICE_ID, AppConstant.KEY_HIGH_VALUE_PROJECT_ACTION_SAVE);
            dataset.put(AppConstant.WORK_ID, work_id);
            dataset.put(AppConstant.INSPECTION_ID, inspection_id);
            dataset.put(AppConstant.CREATED_IMEI_NO, prefManager.getIMEI());
            dataset.put(AppConstant.DATE_OF_ACTION, date_of_action);
            dataset.put(AppConstant.ACTION_REMARK, action_remark);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String list_sql = "select * from " + DBHelper.IMAGE_GROUP_ID_OFFLINE+" where action_id="+action_id;
        Log.d("sql", list_sql);
        Cursor list = getRawEvents(list_sql, null);

        if (list.getCount() > 0) {
            JSONArray group_array = new JSONArray();
            JSONObject element = new JSONObject();
            if (list.moveToFirst()) {
                do {
                    element = new JSONObject();

                    String id = list.getString(list.getColumnIndexOrThrow("id"));
                    String grouping = list.getString(list.getColumnIndexOrThrow("grouping"));
                    JSONArray arr = null;
                    try {
                        arr = new JSONArray(grouping);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        element.put("id",id);
                        element.put("grouping",arr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    group_array.put(element);
                } while (list.moveToNext());

            }
            try {
                dataset.put("grouping_details", group_array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        JSONArray imageJson = new JSONArray();

        String image_sql = "Select * from " + DBHelper.CAPTURED_PHOTO + " where action_id =" + action_id + " and work_id =" + work_id;
        Log.d("sql", image_sql);
        Cursor image = getRawEvents(image_sql, null);

        if (image.getCount() > 0) {
            int i = 0;
            if (image.moveToFirst()) {
                do {
                    String image_work_id = image.getString(image.getColumnIndexOrThrow(AppConstant.WORK_ID));
                    String image_group_id = image.getString(image.getColumnIndexOrThrow(AppConstant.IMAGE_GROUP_ID));
                    int image_inspection_id = image.getInt(image.getColumnIndexOrThrow(AppConstant.INSPECTION_ID));
                    String latitude = image.getString(image.getColumnIndexOrThrow(AppConstant.LATITUDE));
                    String longitude = image.getString(image.getColumnIndexOrThrow(AppConstant.LONGITUDE));
                    String images = image.getString(image.getColumnIndexOrThrow(AppConstant.IMAGE));
                    String description = image.getString(image.getColumnIndexOrThrow(AppConstant.DESCRIPTION));

                    JSONArray imageArray = new JSONArray();

                    imageArray.put(image_work_id);
                    imageArray.put(image_group_id);
                    imageArray.put(latitude);
                    imageArray.put(longitude);
                    imageArray.put(images.trim());
                    imageArray.put(description);
                    i++;
                    imageJson.put(imageArray);

                } while (image.moveToNext());
            }
        }

        try {
            dataset.put("image_details", imageJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String oof = dataset.toString();
        int maxLogSize = 1000;
        for (int i = 0; i <= oof.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > oof.length() ? oof.length() : end;
            Log.v("oof", oof.substring(start, end));
        }
        if (Utils.isOnline()) {
          pendingLayoutFragment.pending_Sync_Data(dataset);
        } else {
            Utils.showAlert(context, "Turn On Mobile Data To Upload");
        }


    }

    public void deletePending_Action(int position) {

        String work_id = pendingListValues.get(position).getWorkID();
        String inspection_id = String.valueOf(pendingListValues.get(position).getInspectionID());
        String action_id = String.valueOf(pendingListValues.get(position).getActionID());

        int sdsm = db.delete(DBHelper.INSPECTION_ACTION, "inspection_id=? and id=?", new String[]{inspection_id, action_id});
        int sdsm1 = db.delete(DBHelper.CAPTURED_PHOTO, "inspection_id=? and action_id=?", new String[]{inspection_id, action_id});
        pendingListValues.remove(position);
        notifyItemRemoved(position);
        notifyItemChanged(position, pendingListValues.size());
        Log.d("sdsm", String.valueOf(sdsm));
        Dashboard.getPendingCount();
        if (pendingListValues.size() < 1) {
            Dashboard.hidePending();
            closeFunction();
        }

    }

    public void deletePending_Inspection(int position) {

        String work_id = pendingListValues.get(position).getWorkID();
        String inspection_id = String.valueOf(pendingListValues.get(position).getInspectionID());

        int sdsm = db.delete(DBHelper.INSPECTION_PENDING, "inspection_id=? and work_id=?", new String[]{inspection_id, work_id});
        int sdsm1 = db.delete(DBHelper.CAPTURED_PHOTO, "inspection_id=? and work_id=?", new String[]{inspection_id, work_id});
        pendingListValues.remove(position);
        notifyItemRemoved(position);
        notifyItemChanged(position, pendingListValues.size());
        Log.d("sdsm", String.valueOf(sdsm));
        Dashboard.getPendingCount();
        if (pendingListValues.size() < 1) {
            Dashboard.hidePending();
            closeFunction();
        }

    }

    public int getPendingSize(){
        return pendingListValues.size();
    }

    public void closeFunction() {
        context.onBackPressed();
        context.overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

//        pendingListValues.remove(position);
    //  pendingLayoutFragment.notify();


    public static JSONObject dataTobeSavedJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), context.getResources().getString(R.string.init_vector), dataset.toString().replaceAll(" ", ""));
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("saving", "" + authKey);
        return dataSet;
    }


    public Cursor getRawEvents(String sql, String string) {
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }

}
