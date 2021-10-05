package com.nic.Inspection.Activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.nic.Inspection.Adapter.ImageDescriptionAdapter;
import com.nic.Inspection.Adapter.InspectionListAdapter;
import com.nic.Inspection.DataBase.DBHelper;
import com.nic.Inspection.Model.BlockListValue;
import com.nic.Inspection.R;
import com.nic.Inspection.Support.MyCustomTextView;
import com.nic.Inspection.Utils.Utils;
import com.nic.Inspection.constant.AppConstant;
import com.nic.Inspection.session.PrefManager;

import java.util.ArrayList;
import java.util.List;

import static com.nic.Inspection.Activity.LoginScreen.db;

/**
 * Created by AchanthiSundar on 08-01-2019.
 */

public class ViewInspectionReportScreen extends AppCompatActivity implements View.OnClickListener {
    private ScrollView scrollView;
    private MyCustomTextView action_tv;
    private List<View> viewArrayList = new ArrayList<>();

    private Context context;

    final int CAMERA_REQUEST = 1888;

    ImageView imageView;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 2500;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;

    // key to store image path in savedInstance state
    public static final String KEY_IMAGE_STORAGE_PATH = "image_path";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // Bitmap sampling size
    public static final int BITMAP_SAMPLE_SIZE = 8;

    // Gallery directory name to store the images or videos
    public static final String GALLERY_DIRECTORY_NAME = "Hello Camera";

    // Image and Video file extensions
    public static final String IMAGE_EXTENSION = "jpg";
    public static final String VIDEO_EXTENSION = "mp4";

    private static String imageStoragePath;
    private ImageView back_img,homeimg;
    private MyCustomTextView district_tv, scheme_name_tv, block_name_tv, block_user_tv, village_name_tv, fin_year_tv, title_tv;
    private MyCustomTextView projectName, amountTv, levelTv;
    private LinearLayout village_layout, block_layout;
    private ImageDescriptionAdapter imageAdapter;
    private InspectionListAdapter inspectionListAdapter;
    private RecyclerView imageRecyclerView, inspectionListRecyclerView;
    PrefManager prefManager;
    private ArrayList<BlockListValue> imagelistValues = new ArrayList<>();
    private ArrayList<BlockListValue> inspectionlistvalues = new ArrayList<>();


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_inspection_report_with_search);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        intializeUI();
    }

    public void intializeUI() {
        prefManager = new PrefManager(this);
        village_layout = (LinearLayout) findViewById(R.id.village_layout);
        block_layout = (LinearLayout) findViewById(R.id.block_user_layout);
        block_user_tv = (MyCustomTextView) findViewById(R.id.block_user_tv);
        district_tv = (MyCustomTextView) findViewById(R.id.district_tv);
        scheme_name_tv = (MyCustomTextView) findViewById(R.id.scheme_name_tv);
        block_name_tv = (MyCustomTextView) findViewById(R.id.block_name_tv);
        village_name_tv = (MyCustomTextView) findViewById(R.id.village_name_tv);
        fin_year_tv = (MyCustomTextView) findViewById(R.id.fin_year_tv);

        projectName = (MyCustomTextView) findViewById(R.id.project_title_tv);
        amountTv = (MyCustomTextView) findViewById(R.id.amount_tv);
        levelTv = (MyCustomTextView) findViewById(R.id.level_tv);
        title_tv = (MyCustomTextView) findViewById(R.id.title_tv);

        scrollView = (ScrollView) findViewById(R.id.scroll_view);
//        action_tv = (MyCustomTextView) findViewById(R.id.action_tv);
        back_img = (ImageView) findViewById(R.id.backimg);
        homeimg = (ImageView) findViewById(R.id.homeimg);
        inspectionListRecyclerView = (RecyclerView) findViewById(R.id.image_list_with_description);

        district_tv.setText(prefManager.getDistrictName());
        scheme_name_tv.setText(prefManager.getSchemeName());
        block_name_tv.setText(prefManager.getBlockName());
        fin_year_tv.setText(prefManager.getFinancialyearName());

        projectName.setText(getIntent().getStringExtra(AppConstant.WORK_NAME));
        amountTv.setText(getIntent().getStringExtra(AppConstant.AS_AMOUNT));
        levelTv.setText(getIntent().getStringExtra(AppConstant.WORK_SATGE_NAME));

        back_img.setOnClickListener(this);
        homeimg.setOnClickListener(this);
//        action_tv.setOnClickListener(this);
        // imageAdapter = new ImageDescriptionAdapter(this,imagelistValues );

        inspectionListAdapter = new InspectionListAdapter(this, inspectionlistvalues);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        inspectionListRecyclerView.setLayoutManager(mLayoutManager);
        inspectionListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        inspectionListRecyclerView.setHasFixedSize(true);
        inspectionListRecyclerView.setFocusable(false);
        inspectionListRecyclerView.setNestedScrollingEnabled(false);

        if (prefManager.getLevels().equalsIgnoreCase("B")) {
            village_layout.setVisibility(View.VISIBLE);
            village_name_tv.setText(prefManager.getVillageListPvName());
            block_layout.setVisibility(View.VISIBLE);
            block_user_tv.setText(prefManager.getBlockName());
        }
        if(!prefManager.getLevels().equalsIgnoreCase("B")) {
            title_tv.setText("View Inspected Work Details");
        }
        else{
            title_tv.setText("Record Action Taken");
        }
        // retrievedata();
        retrievedata_inspection();
        //  inspectionListRecyclerView.setAdapter(inspectionListAdapter);

    }


    private void retrievedata_inspection() {
        inspectionlistvalues.clear();
        String workId = getIntent().getStringExtra(AppConstant.WORK_ID);

        // String inspection_sql = "select * from (select * from "+DBHelper.INSPECTION+" WHERE work_id="+workId+")a left join (select * from captured_photo)b on a.inspection_id=b.inspection_id and a.work_id=b.work_id group by a.inspection_id";
        //String inspection_sql = "select a.inspection_id as inspection_id,a.id as id,a.work_id as work_id,a.date_of_inspection as date_of_inspection,a.inspection_remark as inspection_remark,b.observation as observation from(select * from INSPECTION WHERE id in (select inspection_id from captured_photo))a left join (select * from observation)b on a.observation = b.id where work_id ="+workId ;
        String inspection_sql = "select a.inspection_id as inspection_id,a.id as id,a.work_id as work_id,a.date_of_inspection as date_of_inspection,a.inspection_remark as inspection_remark,b.observation as observation,a.name as inspected_officer,a.desig_name as designation from(select * from INSPECTION WHERE id in (select inspection_id from captured_photo))a left join (select * from observation)b on a.observation = b.id  where a.work_id =" + workId;
        Log.d("inspection_sql", inspection_sql);
        Cursor inspectionList = getRawEvents(inspection_sql, null);

        if (inspectionList.getCount() > 0) {
            if (inspectionList.moveToFirst()) {
                do {
                    String work_id = inspectionList.getString(inspectionList.getColumnIndexOrThrow(AppConstant.WORK_ID));
                    String id = inspectionList.getString(inspectionList.getColumnIndexOrThrow("id"));
                    String date_of_inspection = inspectionList.getString(inspectionList.getColumnIndexOrThrow(AppConstant.DATE_OF_INSPECTION));
                    String inspection_remark = inspectionList.getString(inspectionList.getColumnIndexOrThrow(AppConstant.INSPECTION_REMARK));
                    String observation = inspectionList.getString(inspectionList.getColumnIndexOrThrow(AppConstant.OBSERVATION));
//                    String dist_action = inspectionList.getString(inspectionList.getColumnIndexOrThrow(AppConstant.DISTRICT_ACTION));
//                    String state_action = inspectionList.getString(inspectionList.getColumnIndexOrThrow(AppConstant.STATE_ACTION));
//                    String sub_div_action = inspectionList.getString(inspectionList.getColumnIndexOrThrow(AppConstant.SUB_DIV_ACTION));
                    String inspected_officer = inspectionList.getString(inspectionList.getColumnIndexOrThrow("inspected_officer"));
                    String designation = inspectionList.getString(inspectionList.getColumnIndexOrThrow("designation"));
                    int inspection_id = inspectionList.getInt(inspectionList.getColumnIndexOrThrow(AppConstant.INSPECTION_ID));

                    BlockListValue inspectionValue = new BlockListValue();
                    inspectionValue.setWorkID(work_id);
                    Log.d("inspectworkId", "" + id);
                    inspectionValue.setDate_of_inspection(date_of_inspection);
                    inspectionValue.setInspection_remark(inspection_remark);
                    inspectionValue.setObservation(observation);
                    inspectionValue.setInspectionID(inspection_id);
                    inspectionValue.setOnlineInspectID(id);
                    inspectionValue.setDetail("Online");
                    inspectionValue.setInspectedOffName(inspected_officer);
                    inspectionValue.setInspectedOffDesignName(designation);

//                    if (dist_action == null && state_action == null && sub_div_action == null) {
//                        inspectionValue.setActionresult("NO Action Taken Yet");
//                    } else if (dist_action == "1" && state_action == "1" && sub_div_action == "1") {
//                        inspectionValue.setActionresult("Accepted");
//                    } else {
//                        inspectionValue.setActionresult("Pending");
//                    }


                    inspectionlistvalues.add(inspectionValue);

                } while (inspectionList.moveToNext());
            }
        }
        retrievePendingdata();
        // if ((!(inspectionlistvalues.size() < 1)) && (inspectionValue.getWorkID().equalsIgnoreCase(prefManager.getKeyActionWorkid()))) {
        if ((!(inspectionlistvalues.size() < 1))) {
            inspectionListRecyclerView.setAdapter(inspectionListAdapter);
            Log.d("size", String.valueOf(inspectionlistvalues.size()));
        }
    }

    private void retrievePendingdata() {
        String level = "";
        if(prefManager.getLevels().equalsIgnoreCase("D")) {
            level = "D";
        }else if(prefManager.getLevels().equalsIgnoreCase("S")) {
            level = "S";
        }
        String workId = getIntent().getStringExtra(AppConstant.WORK_ID);
        String pendingList_sql = "select * from(select * from " + DBHelper.INSPECTION_PENDING + " WHERE inspection_id in (select inspection_id from " + DBHelper.CAPTURED_PHOTO + "))a left join (select * from " + DBHelper.OBSERVATION_TABLE + ")b on a.observation = b.id where delete_flag = 0 and level='"+level+"' and inspection_remark != '' and work_id ="+workId;
        Log.d("sql", pendingList_sql);
        Cursor pendingList = getRawEvents(pendingList_sql, null);

        if (pendingList.getCount() > 0) {
            if (pendingList.moveToFirst()) {
                do {
                    String work_id = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.WORK_ID));
                    int inspection_id = pendingList.getInt(pendingList.getColumnIndexOrThrow(AppConstant.INSPECTION_ID));
                    String stage_of_work_on_inspection = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.STAGE_OF_WORK_ON_INSPECTION));
                    String stage_of_work_on_inspection_name = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.STAGE_OF_WORK_ON_INSPECTION_NAME));
                    String date_of_inspection = Utils.formatDate(pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.DATE_OF_INSPECTION)));
                    int observation = pendingList.getInt(pendingList.getColumnIndexOrThrow(AppConstant.OBSERVATION_ID));
                    String inspection_remark = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.INSPECTION_REMARK));
                    String created_date = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.CREATED_DATE));
                    String created_ipaddress = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.CREATED_IMEI_NO));
                    String created_username = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.CREATED_USER_NAME));
                    String Observation = pendingList.getString(pendingList.getColumnIndexOrThrow(AppConstant.OBSERVATION_NAME));


                    BlockListValue inspectionValue = new BlockListValue();
                    inspectionValue.setWorkID(work_id);

                    inspectionValue.setDate_of_inspection(date_of_inspection);
                    inspectionValue.setInspection_remark(inspection_remark);
                    inspectionValue.setObservation(Observation);
                    inspectionValue.setInspectionID(inspection_id);
                    inspectionValue.setOnlineInspectID(String.valueOf(inspection_id));
                    inspectionValue.setDetail("Offline");
                    inspectionValue.setInspectedOffName(prefManager.getInspectedOfficerName());
                    inspectionValue.setInspectedOffDesignName(prefManager.getInspectedOfficerDesignation());
                    inspectionlistvalues.add(inspectionValue);


                } while (pendingList.moveToNext());
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backimg:
                onBackPress();
                break;
            case R.id.homeimg :
                dashboard();
                break;
        }
    }

    public void dashboard() {
        Intent intent = new Intent(this, Dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    private final void focusOnView(final ScrollView your_scrollview, final MyCustomTextView your_EditBox) {
        your_scrollview.post(new Runnable() {
            @Override
            public void run() {
                your_scrollview.fullScroll(View.FOCUS_DOWN);
                //your_scrollview.scrollTo(0, your_EditBox.getY());
            }
        });
    }

    public void onBackPress() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        //Intent intent = new Intent(this, HomeScreenActivity.class);
        //startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);

    }

    public Cursor getRawEvents(String sql, String string) {
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }


}
