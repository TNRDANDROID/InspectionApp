package com.nic.Inspection.Fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.nic.Inspection.Activity.Dashboard;
import com.nic.Inspection.Activity.LoginScreen;
import com.nic.Inspection.Adapter.PendingLayoutAdapter;
import com.nic.Inspection.DataBase.DBHelper;
import com.nic.Inspection.Model.BlockListValue;
import com.nic.Inspection.R;
import com.nic.Inspection.Support.MyCustomLayoutManager;
import com.nic.Inspection.Support.MyCustomTextView;
import com.nic.Inspection.Utils.UrlGenerator;
import com.nic.Inspection.Utils.Utils;
import com.nic.Inspection.api.Api;
import com.nic.Inspection.api.ApiService;
import com.nic.Inspection.api.ServerResponse;
import com.nic.Inspection.constant.AppConstant;
import com.nic.Inspection.session.PrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.nic.Inspection.Activity.LoginScreen.db;

public class PendingLayoutFragment extends Fragment implements View.OnClickListener,Api.ServerResponseListener {

    private PrefManager prefManager;
    private String cacheValue = "cache";
    private RecyclerView pending_recycler_view;
    private ArrayList<BlockListValue> pendingListValues = new ArrayList<>();

    private JSONArray updatedJsonArray;
    private Context context;
    private static PendingLayoutAdapter pendingLayoutAdapter;
    private MyCustomTextView not_found_tv,title_tv;
    private ImageView homeIcon;


    public PendingLayoutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.pending_layout_screen, container, false);
        initializeUI(view);
        return view;
    }

    public void initializeUI(View view) {

        prefManager = new PrefManager(getActivity());

        pending_recycler_view = (RecyclerView) view.findViewById(R.id.pending_recycler_view);
        not_found_tv = (MyCustomTextView) view.findViewById(R.id.not_found_tv);
        title_tv = (MyCustomTextView) view.findViewById(R.id.title_tv);
        homeIcon = (ImageView) view.findViewById(R.id.home);
        homeIcon.setOnClickListener(this);
        MyCustomLayoutManager mLayoutManager = new MyCustomLayoutManager(getActivity());
        pending_recycler_view.setLayoutManager(mLayoutManager);
        pendingLayoutAdapter = new PendingLayoutAdapter(getActivity(), pendingListValues,this );
        pending_recycler_view.setAdapter(pendingLayoutAdapter);
        pending_recycler_view.setNestedScrollingEnabled(false);
        if (prefManager.getLevels().equalsIgnoreCase("D") || prefManager.getLevels().equalsIgnoreCase("S") ) {
            retrievePendingdata_Inspection();
        }else if(prefManager.getLevels().equalsIgnoreCase("B")) {
            retrievePendingdata_Action();
        }
        title_tv.setText("Pending Screen");


    }
    public void retrievePendingdata_Inspection() {
        pendingListValues.clear();
        String level="";
        if(prefManager.getLevels().equalsIgnoreCase("D")) {
            level = "D";
        }else if(prefManager.getLevels().equalsIgnoreCase("S")) {
            level = "S";
        }
        String pendingList_sql = "select * from(select * from "+DBHelper.INSPECTION_PENDING +" WHERE inspection_id in (select inspection_id from "+DBHelper.CAPTURED_PHOTO+"))a left join (select * from "+DBHelper.OBSERVATION_TABLE+")b on a.observation = b.id where delete_flag = 0 and inspection_remark != '' and level ='"+level+"'";
        Log.d("pendingList_sql", pendingList_sql);
        Cursor pendingList = getRawEvents(pendingList_sql, null);

        if (pendingList.getCount() > 0) {
            if (pendingList.moveToFirst()) {
                do {
                    String work_id = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.WORK_ID));
                    int inspection_id = pendingList.getInt(pendingList.getColumnIndexOrThrow(AppConstant.INSPECTION_ID));
                    String stage_of_work_on_inspection = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.STAGE_OF_WORK_ON_INSPECTION));
                    String stage_of_work_on_inspection_name = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.STAGE_OF_WORK_ON_INSPECTION_NAME));
                    String date_of_inspection = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.DATE_OF_INSPECTION));
                    int observation = pendingList.getInt(pendingList.getColumnIndexOrThrow(AppConstant.OBSERVATION_ID));
                    String inspection_remark = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.INSPECTION_REMARK));
                    String created_date = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.CREATED_DATE));
                    String created_ipaddress = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.CREATED_IMEI_NO));
                    String created_username = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.CREATED_USER_NAME));
                    String Observation = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.OBSERVATION_NAME));
                    String dcode = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.DISTRICT_CODE));
                    String ae_username = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.AE_USERNAME));


                    BlockListValue pendingListValue = new BlockListValue();
                    pendingListValue.setWorkID(work_id);
                    pendingListValue.setInspectionID(inspection_id);
                    pendingListValue.setWorkStageCode(stage_of_work_on_inspection);
                    pendingListValue.setWorkStageName(stage_of_work_on_inspection_name);
                    pendingListValue.setDate_of_inspection(date_of_inspection);
                    pendingListValue.setObservationID(observation);
                    pendingListValue.setInspection_remark(inspection_remark);
                    pendingListValue.setCreatedDate(created_date);
                    pendingListValue.setCreatedUserName(created_username);
                    pendingListValue.setCreatedIpAddress(created_ipaddress);
                    pendingListValue.setWorkStageName(stage_of_work_on_inspection_name);
                    pendingListValue.setObservation(Observation);
                    pendingListValue.setAEUserName(ae_username);
                    if(prefManager.getLevels().equalsIgnoreCase("S")) {
                        pendingListValue.setDistictCode(dcode);
                    }
                    pendingListValues.add(pendingListValue);

                } while (pendingList.moveToNext());
            }
        }

        if (!(pendingListValues.size() < 1)) {
            Dashboard.getPendingCount();
            pending_recycler_view.setAdapter(pendingLayoutAdapter);

        } else {
            //not_found_tv.setVisibility(View.VISIBLE);
            Dashboard.getPendingCount();
        }

    }

    public void retrievePendingdata_Action() {
        pendingListValues.clear();
        String pendingList_sql = "select * from " + DBHelper.INSPECTION_ACTION + " WHERE id in (select action_id from captured_photo) and delete_flag = 0 and action_remark != ''";
        Log.d("pendingList_sql", pendingList_sql);
        Cursor pendingList = getRawEvents(pendingList_sql, null);

        if (pendingList.getCount() > 0) {
            if (pendingList.moveToFirst()) {
                do {
                    String work_id = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.WORK_ID));
                    int inspection_id = pendingList.getInt(pendingList.getColumnIndexOrThrow(AppConstant.INSPECTION_ID));
                    String date_of_action = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.DATE_OF_ACTION));
                    String action_remark = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.ACTION_REMARK));
                    int action_id = pendingList.getInt(pendingList.getColumnIndexOrThrow("id"));


                    BlockListValue pendingListValue = new BlockListValue();
                    pendingListValue.setWorkID(work_id);
                    pendingListValue.setInspectionID(inspection_id);
                    pendingListValue.setActionID(action_id);
                    pendingListValue.setDate_of_Action(date_of_action);
                    pendingListValue.setAction_remark(action_remark);

                    pendingListValues.add(pendingListValue);

                } while (pendingList.moveToNext());
            }
        }

        if (!(pendingListValues.size() < 1)) {
            Dashboard.getPendingCount();
            pending_recycler_view.setAdapter(pendingLayoutAdapter);

        } else {
           not_found_tv.setVisibility(View.VISIBLE);
            Dashboard.getPendingCount();
        }

    }


    void startActivity( ) {


            Intent intent = new Intent(getActivity(), Dashboard.class);

            getActivity().startActivityForResult(intent, 1);
            getActivity().overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                String cameback="CameBack";
                Intent intent = new Intent(getActivity(),Dashboard.class);
                intent.putExtra("Comingback", cameback);
                startActivity(intent);
                return true;
        }
        return false;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home:
                 dashboard();
                break;
        }
    }
public void dashboard(){
    Intent intent = new Intent(getActivity(),Dashboard.class);
    startActivity(intent);
    getActivity().overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
}
//    public void pending_Sync_Data() {
//        try {
//            new ApiService(getActivity()).makeJSONObjectRequest("pendingSaveData", Api.Method.POST, UrlGenerator.getInspectionServicesListUrl(), pendingLayoutAdapter.dataTobeSavedJsonParams(), "not cache",  this);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
    public JSONObject pending_Sync_Data(JSONObject dataset) {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(),getResources().getString(R.string.init_vector),dataset.toString());
        JSONObject savedDataSet = new JSONObject();
        try {
            savedDataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
            savedDataSet.put(AppConstant.DATA_CONTENT, authKey);

            new ApiService(getActivity()).makeJSONObjectRequest("pendingSaveData", Api.Method.POST, UrlGenerator.getInspectionServicesListUrl(), savedDataSet, "not cache", this);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return savedDataSet;
    }


    @Override
    public void OnMyResponse(ServerResponse serverResponse) {
        try {
            String urlType = serverResponse.getApi();
            JSONObject responseObj = serverResponse.getJsonResponse();

            if ("pendingSaveData".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String responseDecryptedBlockKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                JSONObject jsonObject = new JSONObject(responseDecryptedBlockKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                    // loadBlockList(jsonObject.getJSONArray(AppConstant.JSON_DATA));
                    if (prefManager.getLevels().equalsIgnoreCase("D") || prefManager.getLevels().equalsIgnoreCase("S") ) {
                        db.delete(DBHelper.INSPECTION_PENDING,"inspection_id=?",new String[] {prefManager.getKeyDeleteId()});
                        retrievePendingdata_Inspection();
                    }else if(prefManager.getLevels().equalsIgnoreCase("B")) {
                        db.delete(DBHelper.INSPECTION_ACTION,"id=?",new String[] {prefManager.getKeyDeleteId()});
                        retrievePendingdata_Action();
                    }
                    getInspectionList_blockwise();
                    getInspectionList_Images_blockwise();
                    getAction_ForInspection();
                    getActionImages();

                    pendingLayoutAdapter.notifyDataSetChanged();

                    Utils.showAlert(getActivity(), "Uploaded");
                }
                Log.d("saved_response", "" + responseDecryptedBlockKey);
            }
            Dashboard.getPendingCount();
            if(pendingLayoutAdapter.getPendingSize() <1){
                Dashboard.hidePending();
            }
            if ("InspectionListBlockWise".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String responseDecryptedKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                JSONObject jsonObject = new JSONObject(responseDecryptedKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                    Insert_inspectionList(jsonObject.getJSONArray(AppConstant.JSON_DATA));
                } else if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("NO_RECORD")) {
                    // Utils.showAlert(this, "No Record Found");
                }
                Log.d("InspectionListBlockWise", "" + jsonObject.getJSONArray(AppConstant.JSON_DATA));

            }
            if ("InspectionListBlockWise_Images".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String responseDecryptedKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                JSONObject jsonObject = new JSONObject(responseDecryptedKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                    Insert_inspectionList_Images(jsonObject.getJSONArray(AppConstant.JSON_DATA));
                } else if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("NO_RECORD")) {
                    // Utils.showAlert(this, "No Record Found");
                    Log.d("responseInspect_Action", jsonObject.getString("MESSAGE"));
                }
                Log.d("response_Images", "" + jsonObject.getJSONArray(AppConstant.JSON_DATA));

            }
            if ("InspectionListBlockWise_Action".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String responseDecryptedKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                JSONObject jsonObject = new JSONObject(responseDecryptedKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                    Insert_inspectionList_Action(jsonObject.getJSONArray(AppConstant.JSON_DATA));
                } else if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("NO_RECORD")) {
                    // Utils.showAlert(this, "No Record Found");
                    Log.d("responseInspect_Action", jsonObject.getString("MESSAGE"));
                }
                Log.d("responseInspect_Action", "" + jsonObject.getJSONArray(AppConstant.JSON_DATA));

            }

            if ("ActionImages".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String responseDecryptedKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                JSONObject jsonObject = new JSONObject(responseDecryptedKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                    Insert_ActionList_Images(jsonObject.getJSONArray(AppConstant.JSON_DATA));
                } else if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("NO_RECORD")) {
                    // Utils.showAlert(this, "No Record Found");
                    Log.d("ActionImages", jsonObject.getString("MESSAGE"));
                }
                Log.d("ActionImages", "" + jsonObject.getJSONArray(AppConstant.JSON_DATA));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public Cursor getRawEvents(String sql, String string) {
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }

    @Override
    public void OnError(VolleyError volleyError) {

    }

    public void getInspectionList_blockwise() {
        try {
            new ApiService(this.context).makeJSONObjectRequest("InspectionListBlockWise", Api.Method.POST, UrlGenerator.getInspectionServicesListUrl(), InspectionListBlockwiseJsonParams(), "not cache", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getInspectionList_Images_blockwise() {
        try {
            new ApiService(this.context).makeJSONObjectRequest("InspectionListBlockWise_Images", Api.Method.POST, UrlGenerator.getInspectionServicesListUrl(), InspectionListImageJsonParams(), "not cache", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getAction_ForInspection() {
        try {
            new ApiService(this.context).makeJSONObjectRequest("InspectionListBlockWise_Action", Api.Method.POST, UrlGenerator.getInspectionServicesListUrl(), InspectionListActionJsonParams(), "not cache", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getActionImages() {
        try {
            new ApiService(this.context).makeJSONObjectRequest("ActionImages", Api.Method.POST, UrlGenerator.getInspectionServicesListUrl(), ActionImagesJsonParams(), "not cache", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject InspectionListBlockwiseJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), Utils.InspectionListblockWise(getActivity()).toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("InspectionList", "" + authKey);
        return dataSet;
    }

    public JSONObject InspectionListImageJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), Utils.InspectionList_Image(getActivity()).toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("InspectionList_Image", "" + authKey);
        return dataSet;
    }

    public JSONObject InspectionListActionJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), Utils.InspectionList_Action(getActivity()).toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("InspectionList_Action", "" + authKey);
        return dataSet;
    }

    public JSONObject ActionImagesJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), Utils.ActionImages(getActivity()).toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("Action_Images", "" + authKey);
        return dataSet;
    }

    private void Insert_inspectionList(JSONArray jsonArray) {
        try {
            //db.rawQuery("DELETE FROM "+DBHelper.INSPECTION+" WHERE delete_flag =1",null);
            db.delete(DBHelper.INSPECTION, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String workID = jsonArray.getJSONObject(i).getString(AppConstant.WORK_ID);
                    String id = jsonArray.getJSONObject(i).getString("id");
                    String stageOfWorkOnInspection = jsonArray.getJSONObject(i).getString(AppConstant.STAGE_OF_WORK_ON_INSPECTION);
                    String dateOfInspection = Utils.formatDate(jsonArray.getJSONObject(i).getString(AppConstant.DATE_OF_INSPECTION));
                    String inspectedBy = jsonArray.getJSONObject(i).getString(AppConstant.INSPECTED_BY);
                    String observation = jsonArray.getJSONObject(i).getString(AppConstant.OBSERVATION);
                    String inspectionRemark = jsonArray.getJSONObject(i).getString(AppConstant.INSPECTION_REMARK);
                    String inspected_officer = jsonArray.getJSONObject(i).getString(AppConstant.INSPECTED_USER_NAME);
                    String designation = jsonArray.getJSONObject(i).getString(AppConstant.INSPECTED_DESIGATION_NAME);


                    ContentValues getInspectionList = new ContentValues();

                    getInspectionList.put(AppConstant.WORK_ID, workID);
                    getInspectionList.put("id", id);
                    getInspectionList.put(AppConstant.STAGE_OF_WORK_ON_INSPECTION, stageOfWorkOnInspection);
                    getInspectionList.put(AppConstant.DATE_OF_INSPECTION, dateOfInspection);
                    getInspectionList.put(AppConstant.INSPECTED_BY, inspectedBy);
                    getInspectionList.put(AppConstant.OBSERVATION, observation);
                    getInspectionList.put(AppConstant.INSPECTION_REMARK, inspectionRemark);
                    getInspectionList.put(AppConstant.INSPECTED_USER_NAME, inspected_officer);
                    getInspectionList.put(AppConstant.INSPECTED_DESIGATION_NAME, designation);
                    getInspectionList.put("delete_flag", 1);


                    LoginScreen.db.insert(DBHelper.INSPECTION, null, getInspectionList);
                }


            } else {
                Utils.showAlert(this.context, "No Record Found!");
            }

        } catch (JSONException j) {
            j.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException a) {
            a.printStackTrace();
        }

    }


    private void Insert_inspectionList_Images(JSONArray jsonArray) {
        try {
            // db.delete(DBHelper.CAPTURED_PHOTO, null, null);
            db.execSQL(String.format("DELETE FROM " + DBHelper.CAPTURED_PHOTO + " WHERE pending_flag IS NULL OR trim(pending_flag) = '';", null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String inspection_id = jsonArray.getJSONObject(i).getString(AppConstant.INSPECTION_ID);
                    String image = jsonArray.getJSONObject(i).getString(AppConstant.IMAGE);
                    String image_description = jsonArray.getJSONObject(i).getString("image_description");
                    String image_id = jsonArray.getJSONObject(i).getString(AppConstant.IMAGE_ID);


                    ContentValues Imageist = new ContentValues();
                    Imageist.put(AppConstant.IMAGE_ID, image_id);
                    Imageist.put(AppConstant.INSPECTION_ID, inspection_id);
                    Imageist.put(AppConstant.IMAGE, image);
                    Imageist.put(AppConstant.DESCRIPTION, image_description);

                    LoginScreen.db.insert(DBHelper.CAPTURED_PHOTO, null, Imageist);
                }

            } else {
                Utils.showAlert(this.context, "No Record Found");
            }

        } catch (JSONException j) {
            j.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException a) {
            a.printStackTrace();
        }
    }

    private void Insert_inspectionList_Action(JSONArray jsonArray) {
        try {
            db.execSQL(String.format("DELETE FROM " + DBHelper.INSPECTION_ACTION + " WHERE delete_flag=1;", null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            updatedJsonArray = new JSONArray();
            updatedJsonArray = jsonArray;
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String workID = jsonArray.getJSONObject(i).getString(AppConstant.WORK_ID);
                    //  String id = jsonArray.getJSONObject(i).getString("id");
                    String onlineaction_id = jsonArray.getJSONObject(i).getString("id");
                    String inspection_id = jsonArray.getJSONObject(i).getString(AppConstant.INSPECTION_ID);
                    String date_of_action = jsonArray.getJSONObject(i).getString(AppConstant.DATE_OF_ACTION);
                    String action_taken = jsonArray.getJSONObject(i).getString(AppConstant.ACTION_TAKEN);
                    String action_remark = jsonArray.getJSONObject(i).getString(AppConstant.ACTION_REMARK);
                    String dist_action = jsonArray.getJSONObject(i).getString(AppConstant.DISTRICT_ACTION);
                    String state_action = jsonArray.getJSONObject(i).getString(AppConstant.STATE_ACTION);
                    String sub_div_action = jsonArray.getJSONObject(i).getString(AppConstant.SUB_DIV_ACTION);
                    String action_taken_officer = jsonArray.getJSONObject(i).getString(AppConstant.ACTION_TAKEN_OFFICER);
                    String action_taken_officer_desig = jsonArray.getJSONObject(i).getString(AppConstant.ACTION_TAKEN_OFFICER_DESIGNATION);

                    ContentValues ActionList = new ContentValues();

                    ActionList.put(AppConstant.WORK_ID, workID);
                    ActionList.put(AppConstant.ACTION_ID, onlineaction_id);
                    //   ActionList.put("id", id);
                    ActionList.put(AppConstant.INSPECTION_ID, inspection_id);
                    ActionList.put(AppConstant.DATE_OF_ACTION, date_of_action);
                    ActionList.put(AppConstant.ACTION_TAKEN, action_taken);
                    ActionList.put(AppConstant.ACTION_REMARK, action_remark);
                    ActionList.put(AppConstant.ACTION_TAKEN_OFFICER, action_taken_officer);
                    ActionList.put(AppConstant.ACTION_TAKEN_OFFICER_DESIGNATION, action_taken_officer_desig);
                    ActionList.put(AppConstant.DISTRICT_ACTION, dist_action);
                    ActionList.put(AppConstant.STATE_ACTION, state_action);
                    ActionList.put(AppConstant.SUB_DIV_ACTION, sub_div_action);
                    ActionList.put(AppConstant.DELETE_FLAG, "1");

                    LoginScreen.db.insert(DBHelper.INSPECTION_ACTION, null, ActionList);
                }

            } else {
                Utils.showAlert(this.context, "No Record Found!");
            }

        } catch (JSONException j) {
            j.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException a) {
            a.printStackTrace();
        }

    }

    private void Insert_ActionList_Images(JSONArray jsonArray) {
        try {
            // db.delete(DBHelper.CAPTURED_PHOTO, null, null);
            db.execSQL(String.format("DELETE FROM " + DBHelper.ACTION_PHOTO, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            updatedJsonArray = new JSONArray();
            updatedJsonArray = jsonArray;
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String inspection_id = jsonArray.getJSONObject(i).getString(AppConstant.INSPECTION_ID);
                    String action_id = jsonArray.getJSONObject(i).getString("action_id");
                    String image_group_id = jsonArray.getJSONObject(i).getString("inspection_img_group_id");
                    String description = jsonArray.getJSONObject(i).getString("action_image_description");
                    String image = jsonArray.getJSONObject(i).getString("image");


                    ContentValues Imageist = new ContentValues();
                    Imageist.put(AppConstant.INSPECTION_ID, Integer.parseInt(inspection_id));
                    Imageist.put(AppConstant.ACTION_ID, Integer.parseInt(action_id));
                    Imageist.put(AppConstant.IMAGE, image);
                    Imageist.put(AppConstant.DESCRIPTION, description);
                    Imageist.put("level","BO");

                    LoginScreen.db.insert(DBHelper.ACTION_PHOTO, null, Imageist);
                }
                // callAlert();

            } else {
                Utils.showAlert(this.context, "No Record Found");
            }

        } catch (JSONException j) {
            j.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException a) {
            a.printStackTrace();
        }

    }
}
