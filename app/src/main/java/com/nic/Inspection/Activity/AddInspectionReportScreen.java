package com.nic.Inspection.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mikelau.croperino.Croperino;
import com.mikelau.croperino.CroperinoConfig;
import com.mikelau.croperino.CroperinoFileUtil;
import com.nic.Inspection.Adapter.CommonAdapter;
import com.nic.Inspection.DataBase.DBHelper;
import com.nic.Inspection.Model.BlockListValue;
import com.nic.Inspection.R;
import com.nic.Inspection.Support.MyCustomTextView;
import com.nic.Inspection.Support.MyLocationListener;
import com.nic.Inspection.Utils.CameraUtils;
import com.nic.Inspection.Utils.FontCache;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static com.nic.Inspection.Activity.LoginScreen.db;

/**
 * Created by NIC on 23-01-2019.
 */

public class AddInspectionReportScreen extends AppCompatActivity implements View.OnClickListener, Api.ServerResponseListener {


    private ScrollView scrollView;
    private MyCustomTextView take_photo,submit;
    private MyCustomTextView district_tv, scheme_name_tv, block_name_tv, fin_year_tv, title_tv;
    private List<View> viewArrayList = new ArrayList<>();
    PrefManager prefManager;

    private static Context context;

    final int CAMERA_REQUEST = 1888;

    ImageView imageView, image_view_preview;
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
    private MyCustomTextView projectName, amountTv, levelTv;
    private Spinner sp_observation, sp_stage ,sp_ae;
    private List<BlockListValue> stageListValues = new ArrayList<>();
    private List<BlockListValue> observationList = new ArrayList<>();
    private List<BlockListValue> AEList = new ArrayList<>();
    private ImageView back_img,homeimg,home;
    LocationManager mlocManager = null;
    LocationListener mlocListener;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int PERMISSION_REQUEST_CODE = 200;

    static ArrayList<String> latitude = new ArrayList<String>();
    static ArrayList<String> longitude = new ArrayList<String>();
    String offlatTextValue, offlanTextValue;
    static String work_id;
    EditText remarkTv;
    static JSONObject dataset;
    private JSONArray updatedJsonArray;
    private SelectBlockSchemeScreen selectBlockSchemeScreen = new SelectBlockSchemeScreen();
    static int inspectionID = 0;
    private List<BlockListValue> imagelistvalues = new ArrayList<>();
    private boolean imageboolean;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.add_inspection_with_toolbar);
        intializeUI();
        viewStage();
        viewObservation();
        viewAE();
    }

    public void intializeUI() {
        scrollView = (ScrollView) findViewById(R.id.scroll_view);
        take_photo = (MyCustomTextView) findViewById(R.id.take_photo);
        submit = (MyCustomTextView) findViewById(R.id.submit);
        prefManager = new PrefManager(this);

        district_tv = (MyCustomTextView) findViewById(R.id.district_tv);
        scheme_name_tv = (MyCustomTextView) findViewById(R.id.scheme_name_tv);
        block_name_tv = (MyCustomTextView) findViewById(R.id.block_name_tv);
        fin_year_tv = (MyCustomTextView) findViewById(R.id.fin_year_tv);

        projectName = (MyCustomTextView) findViewById(R.id.project_title_tv);
        amountTv = (MyCustomTextView) findViewById(R.id.amount_tv);
        levelTv = (MyCustomTextView) findViewById(R.id.level_tv);
        title_tv = (MyCustomTextView) findViewById(R.id.title_tv);
        sp_observation = (Spinner) findViewById(R.id.observation);
        sp_stage = (Spinner) findViewById(R.id.stageSelect);
        sp_ae = (Spinner) findViewById(R.id.sp_ae);
        remarkTv = (EditText) findViewById(R.id.remark);
        remarkTv.setScroller(new Scroller(this));
        remarkTv.setVerticalScrollBarEnabled(true);
        remarkTv.setMovementMethod(new ScrollingMovementMethod());

        back_img = (ImageView) findViewById(R.id.backimg);
        homeimg = (ImageView) findViewById(R.id.homeimg);
        back_img.setOnClickListener(this);
        homeimg.setOnClickListener(this);

        take_photo.setOnClickListener(this);
        submit.setOnClickListener(this);

        district_tv.setText(prefManager.getDistrictName());
        scheme_name_tv.setText(prefManager.getSchemeName());
        block_name_tv.setText(prefManager.getBlockName());
        fin_year_tv.setText(prefManager.getFinancialyearName());
        title_tv.setText("Inspection Observation");

        imageboolean = false;
        new CroperinoConfig("IMG_" + System.currentTimeMillis() + ".jpg", "/MikeLau/Pictures", "/sdcard/MikeLau/Pictures");
        CroperinoFileUtil.setupDirectory(AddInspectionReportScreen.this);

        projectName.setText(getIntent().getStringExtra(AppConstant.WORK_NAME));
        amountTv.setText(getIntent().getStringExtra(AppConstant.AS_AMOUNT));
        levelTv.setText(getIntent().getStringExtra(AppConstant.WORK_SATGE_NAME));
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.take_photo:
                imageWithDescription(take_photo, "mobile", scrollView);
                break;
            case R.id.backimg:
                onBackPress();
                break;
            case R.id.homeimg:
                dashboard();
                break;
            case R.id.home :
                dashboard();
                break;
            case R.id.submit :
                if(imageboolean) {
                    if (!"Select Stage of Work".equalsIgnoreCase(stageListValues.get(sp_stage.getSelectedItemPosition()).getWorkStageName())) {
                        if (!"Select Observation".equalsIgnoreCase(observationList.get(sp_observation.getSelectedItemPosition()).getObservationName())) {
                            if (!"Select AE/Overseer".equalsIgnoreCase(AEList.get(sp_ae.getSelectedItemPosition()).getAEName())) {
                                if (!remarkTv.getText().toString().isEmpty()) {
                                    submit();
                                } else {
                                    Utils.showAlert(this, "Select Remark");
                                }
                            } else {
                                Utils.showAlert(this, "Select AE/Overseer");
                            }
                        } else {
                            Utils.showAlert(this, "Select Observation");
                        }
                    } else {
                        Utils.showAlert(this, "Select Stage of Work");
                    }
                }
                else {
                    Utils.showAlert(this,"Please Take photo");
                }

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

    public void viewStage() {
        stageListValues.clear();
        String workGroupId = getIntent().getStringExtra(AppConstant.WORK_GROUP_ID);
        String workTypeid = getIntent().getStringExtra(AppConstant.WORK_TYPE_ID);

        String sql = "select * from " + DBHelper.WORK_STAGE_TABLE + "  where (work_group_id = " + workGroupId + " and work_type_id = " + workTypeid + ") order by work_stage_order asc";
        Cursor stages = getRawEvents(sql, null);
        Log.d("work_stage_sql", sql);

        BlockListValue stagelist = new BlockListValue();
        stagelist.setWorkStageName("Select Stage of Work");
        stageListValues.add(stagelist);

        if (stages.getCount() > 0) {
            if (stages.moveToFirst()) {
                do {
                    BlockListValue stagelistval = new BlockListValue();
                    String workGroupID = stages.getString(stages.getColumnIndexOrThrow(AppConstant.WORK_GROUP_ID));
                    String workTypeId = stages.getString(stages.getColumnIndexOrThrow(AppConstant.WORK_TYPE_ID));
                    String workStageOrder = stages.getString(stages.getColumnIndexOrThrow(AppConstant.WORK_STAGE_ORDER));
                    String workStageCode = stages.getString(stages.getColumnIndexOrThrow(AppConstant.WORK_STAGE_CODE));
                    String workstageName = stages.getString(stages.getColumnIndexOrThrow(AppConstant.WORK_SATGE_NAME));
                    stagelistval.setWorkGroupID(workGroupID);
                    stagelistval.setWorkTypeID(workTypeId);
                    stagelistval.setWorkStageOrder(workStageOrder);
                    stagelistval.setWorkStageCode(workStageCode);
                    stagelistval.setWorkStageName(workstageName);
                    stageListValues.add(stagelistval);
                } while (stages.moveToNext());
            }
        }
        sp_stage.setAdapter(new CommonAdapter(this, stageListValues, "StageList"));
    }

    public void viewObservation() {
        observationList.clear();
        String sql = "select * from " + DBHelper.OBSERVATION_TABLE;
        Cursor observation = getRawEvents(sql, null);

        BlockListValue observationValue = new BlockListValue();
        observationValue.setObservationName("Select Observation");
        observationList.add(observationValue);

        if (observation.getCount() > 0) {
            if (observation.moveToFirst()) {
                do {
                    BlockListValue observationlistval = new BlockListValue();
                    int obseravtionID = observation.getInt(observation.getColumnIndexOrThrow(AppConstant.OBSERVATION_ID));
                    String observationName = observation.getString(observation.getColumnIndexOrThrow(AppConstant.OBSERVATION_NAME));

                    observationlistval.setObservationID(obseravtionID);
                    observationlistval.setObservationName(observationName);

                    observationList.add(observationlistval);
                } while (observation.moveToNext());
            }
        }

        sp_observation.setAdapter(new CommonAdapter(this, observationList, "ObservationList"));

    }
/*To load AEList spinner*/
    public void viewAE() {
        AEList.clear();

        String sql = "select * from "+DBHelper.AE_OFFICER_LISTS +" where dcode ="+prefManager.getDistrictCode()+" and bcode = "+prefManager.getKeySpinnerSelectedBlockcode() ;
        Cursor stages = getRawEvents(sql, null);
        Log.d("ae_list_sql", sql);

        BlockListValue aelist = new BlockListValue();
        aelist.setAEName("Select AE/Overseer");
        AEList.add(aelist);

        if (stages.getCount() > 0) {
            if (stages.moveToFirst()) {
                do {
                    BlockListValue aelistval = new BlockListValue();
                    String dcode = stages.getString(stages.getColumnIndexOrThrow(AppConstant.DISTRICT_CODE));
                    String bcode = stages.getString(stages.getColumnIndexOrThrow(AppConstant.BLOCK_CODE));
                    String user_name = stages.getString(stages.getColumnIndexOrThrow(AppConstant.KEY_AE_USER_NAME));
                    String name = stages.getString(stages.getColumnIndexOrThrow(AppConstant.KEY_AE_NAME));
                    String desig_name  = stages.getString(stages.getColumnIndexOrThrow(AppConstant.KEY_AE_DESIGNATION_NAME));
                    aelistval.setDistictCode(dcode);
                    aelistval.setBlockCode(bcode);
                    aelistval.setAEUserName(user_name);
                    aelistval.setAEName(name);
                    aelistval.setAEDesignation(desig_name);
                    AEList.add(aelistval);
                } while (stages.moveToNext());
            }
        }
        sp_ae.setAdapter(new CommonAdapter(this, AEList, "AEList"));
    }


    public void imageWithDescription(final MyCustomTextView action_tv, final String type, final ScrollView scrollView) {
        imageboolean = true;
        dataset = new JSONObject();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        work_id = getIntent().getStringExtra(AppConstant.WORK_ID);
        String stage_of_work_on_inspection = stageListValues.get(sp_stage.getSelectedItemPosition()).getWorkStageCode();
        String stage_of_work_on_inspection_name = stageListValues.get(sp_stage.getSelectedItemPosition()).getWorkStageName();
        String date_of_inspection = sdf.format(new Date());
        String inspected_by = "inspected_by";
        int observation = observationList.get(sp_observation.getSelectedItemPosition()).getObservationID();
        String inspection_remark = remarkTv.getText().toString();
        String created_date = date_of_inspection;
        String created_ipaddress = "123214124";
        String created_username = "test";

        if (!Utils.isOnline()) {
            ContentValues inspectionValue = new ContentValues();
            inspectionValue.put(AppConstant.WORK_ID, work_id);
            inspectionValue.put(AppConstant.STAGE_OF_WORK_ON_INSPECTION, stage_of_work_on_inspection);
            inspectionValue.put(AppConstant.STAGE_OF_WORK_ON_INSPECTION_NAME, stage_of_work_on_inspection_name);
            inspectionValue.put(AppConstant.DATE_OF_INSPECTION, date_of_inspection);
            //  inspectionValue.put(AppConstant.INSPECTED_BY,inspected_by);
            inspectionValue.put(AppConstant.OBSERVATION, observation);
            inspectionValue.put(AppConstant.INSPECTION_REMARK, inspection_remark);
            inspectionValue.put(AppConstant.CREATED_DATE, created_date);
            inspectionValue.put(AppConstant.CREATED_IMEI_NO, prefManager.getIMEI());
            inspectionValue.put(AppConstant.CREATED_USER_NAME, prefManager.getUserName());
            inspectionValue.put(AppConstant.DISTRICT_CODE, prefManager.getDistrictCode());
            if(prefManager.getLevels().equalsIgnoreCase("D")) {
                inspectionValue.put("level", "D");
            }else if(prefManager.getLevels().equalsIgnoreCase("S")) {
                inspectionValue.put("level", "S");
            }
            inspectionValue.put("delete_flag", 0);

            LoginScreen.db.insert(DBHelper.INSPECTION_PENDING, null, inspectionValue);
        } else {
            try {
                dataset.put(AppConstant.KEY_SERVICE_ID, AppConstant.KEY_HIGH_VALUE_PROJECT_INSPECTION_SAVE);
                dataset.put(AppConstant.WORK_ID, work_id);
                dataset.put(AppConstant.DATE_OF_INSPECTION, date_of_inspection);
               // dataset.put(AppConstant.INSPECTED_BY, inspected_by);
                dataset.put(AppConstant.CREATED_DATE, created_date);
                dataset.put(AppConstant.CREATED_IMEI_NO, prefManager.getIMEI());
                dataset.put(AppConstant.CREATED_USER_NAME, prefManager.getUserName());
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


        final Dialog dialog = new Dialog(this,
                R.style.AppTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_photo);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        lp.dimAmount = 0.7f;
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.show();


        final LinearLayout mobileNumberLayout = (LinearLayout) dialog.findViewById(R.id.mobile_number_layout);
        MyCustomTextView cancel = (MyCustomTextView) dialog.findViewById(R.id.tv_save_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Button done = (Button) dialog.findViewById(R.id.btn_save_inspection);
        done.setGravity(Gravity.CENTER);
        done.setVisibility(View.VISIBLE);
        done.setTypeface(FontCache.getInstance(this).getFont(FontCache.Font.HEAVY));

        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                JSONArray imageJson = new JSONArray();


                Cursor inpection_Cursor = getRawEvents("SELECT MAX(inspection_id) FROM " + DBHelper.INSPECTION_PENDING, null);
                Log.d("cursor_count", String.valueOf(inpection_Cursor.getCount()));
                if (inpection_Cursor.getCount() > 0) {
                    if (inpection_Cursor.moveToFirst()) {
                        do {
                            inspectionID = inpection_Cursor.getInt(0);
                            Log.d("inspectionID", "" + inspectionID);
                        } while (inpection_Cursor.moveToNext());
                    }
                }
                int childCount = mobileNumberLayout.getChildCount();
                if (childCount > 0) {
                    for (int i = 0; i < childCount; i++) {
                        JSONArray imageArray = new JSONArray();

                        View vv = mobileNumberLayout.getChildAt(i);
                        EditText myEditTextView = (EditText) vv.findViewById(R.id.description);

                        ImageView imageView = (ImageView) vv.findViewById(R.id.image_view);
                        byte[] imageInByte = new byte[0];
                        String image_str = "";
                        try {
                            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                            imageInByte = baos.toByteArray();
                            image_str = Base64.encodeToString(imageInByte, Base64.DEFAULT);
                            // String string = new String(imageInByte);
                            //Log.d("imageInByte_string",string);
                            Log.d("image_str", image_str);
                        } catch (Exception e) {
                            imageboolean = false;
                            Utils.showAlert(AddInspectionReportScreen.this, "Atleast Capture one Photo");
                            break;
                            //e.printStackTrace();
                        }

                        String description = myEditTextView.getText().toString();

                        if (MyLocationListener.latitude > 0) {
                            offlatTextValue = "" + MyLocationListener.latitude;
                            offlanTextValue = "" + MyLocationListener.longitude;
                        }

                        // Toast.makeText(getApplicationContext(),str,Toast.LENGTH_LONG).show();

                        ContentValues imageValue = new ContentValues();

                        imageValue.put(AppConstant.INSPECTION_ID, inspectionID);
                        imageValue.put(AppConstant.WORK_ID, work_id);
                        imageValue.put(AppConstant.LATITUDE, offlatTextValue);
                        imageValue.put(AppConstant.LONGITUDE, offlanTextValue);
                        imageValue.put(AppConstant.IMAGE, image_str.trim());
                        imageValue.put(AppConstant.DESCRIPTION, description);
                        imageValue.put("pending_flag", 1);
                        if(prefManager.getLevels().equalsIgnoreCase("D")) {
                            imageValue.put("level", "D");
                        }else if(prefManager.getLevels().equalsIgnoreCase("S")) {
                            imageValue.put("level", "S");
                        }


                        if (!Utils.isOnline()) {
                            long rowInserted = LoginScreen.db.insert(DBHelper.CAPTURED_PHOTO, null, imageValue);

//                            if (rowInserted != -1) {
//                                Toast.makeText(AddInspectionReportScreen.this, "New Inspection added", Toast.LENGTH_SHORT).show();
//                                Dashboard.getPendingCount();
//                               finish();
//                            } else {
//                                Toast.makeText(AddInspectionReportScreen.this, "Something wrong", Toast.LENGTH_SHORT).show();
//                            }
                        } else {
                            imageArray.put(i);
                            imageArray.put(work_id);
                            imageArray.put(offlatTextValue);
                            imageArray.put(offlanTextValue);
                            imageArray.put(image_str.trim());
                            imageArray.put(description);
                            imageJson.put(imageArray);
                        }

                        long localImageInserted = LoginScreen.db.insert(DBHelper.LOCAL_IMAGE, null, imageValue);
                    }
                    try {
                        dataset.put("image_details", imageJson);

                        Log.d("post_dataset_inspection", dataset.toString());
                     //   String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), dataset.toString());
//                        String authKey = dataset.toString();
//                        int maxLogSize = 1000;
//                        for(int i = 0; i <= authKey.length() / maxLogSize; i++) {
//                            int start = i * maxLogSize;
//                            int end = (i+1) * maxLogSize;
//                            end = end > authKey.length() ? authKey.length() : end;
//                            Log.v("to_send", authKey.substring(start, end));
//                     }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                dialog.dismiss();
                focusOnView(scrollView, action_tv);


            }
        });
        final String values = action_tv.getText().toString().replace("NA", "");
        Button btnAddMobile = (Button) dialog.findViewById(R.id.btn_add);
        btnAddMobile.setTypeface(FontCache.getInstance(this).getFont(FontCache.Font.MEDIUM));
        viewArrayList.clear();
        btnAddMobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageView.getDrawable() != null && viewArrayList.size()>0) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    updateView(AddInspectionReportScreen.this, mobileNumberLayout, "", type);
                } else {
                    Utils.showAlert(AddInspectionReportScreen.this, "First Capture Image then add another Image!");
                }
            }
        });
        if (!values.isEmpty()) {
            String level = "";
            if(prefManager.getLevels().equalsIgnoreCase("D")) {
                level = "D";
            }else if(prefManager.getLevels().equalsIgnoreCase("S")) {
                level = "S";
            }
            Cursor imageList = getRawEvents("SELECT * FROM " + DBHelper.LOCAL_IMAGE +" WHERE level='"+level+"' and work_id="+work_id, null);

            if (imageList.getCount() > 0) {
                imagelistvalues.clear();
                int i =0;

                if (imageList.moveToFirst()) {
                    do {
                        String work_id = imageList.getString(imageList.getColumnIndexOrThrow(AppConstant.WORK_ID));
                        String latitude = imageList.getString(imageList.getColumnIndexOrThrow(AppConstant.LATITUDE));
                        String longitude = imageList.getString(imageList.getColumnIndexOrThrow(AppConstant.LONGITUDE));
                        String description = imageList.getString(imageList.getColumnIndexOrThrow(AppConstant.DESCRIPTION));

                        byte[] photo = imageList.getBlob(imageList.getColumnIndexOrThrow(AppConstant.IMAGE));
                        byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                        BlockListValue imageValue = new BlockListValue();

                        imageValue.setWorkID(work_id);
                        imageValue.setLatitude(latitude);
                        imageValue.setLongitude(longitude);
                        imageValue.setDescription(description);
                        imageValue.setImage(decodedByte);

                        imagelistvalues.add(imageValue);
                        updateView(this, mobileNumberLayout,String.valueOf(i), "localImage");
                        i++;
                    } while (imageList.moveToNext());
                }
                try {
                    db.delete(DBHelper.LOCAL_IMAGE, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


//            if (values.contains(",")) {
//                String[] mobileOrEmail = values.split(",");
//                for (int i = 0; i < mobileOrEmail.length; i++) {
//                    if (viewArrayList.size() < 5) {
//                        updateView(this, mobileNumberLayout, mobileOrEmail[i], type);
//                    }
//                }
//            }
            else {
                if (viewArrayList.size() < 5) {
                    updateView(this, mobileNumberLayout, values, type);
                }
            }
        } else {
            updateView(this, mobileNumberLayout, values, type);
        }
    }

    public  void  submit() {
        try {
            db.delete(DBHelper.LOCAL_IMAGE, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String stage_of_work_on_inspection = stageListValues.get(sp_stage.getSelectedItemPosition()).getWorkStageCode();
        String stage_of_work_on_inspection_name = stageListValues.get(sp_stage.getSelectedItemPosition()).getWorkStageName();
        int observation = observationList.get(sp_observation.getSelectedItemPosition()).getObservationID();
        String ae_username = AEList.get(sp_ae.getSelectedItemPosition()).getAEUserName();
        String inspection_remark = remarkTv.getText().toString();

        if (!Utils.isOnline()) {
            ContentValues inspectionValue = new ContentValues();
            inspectionValue.put(AppConstant.WORK_ID, work_id);
            inspectionValue.put(AppConstant.STAGE_OF_WORK_ON_INSPECTION, stage_of_work_on_inspection);
            inspectionValue.put(AppConstant.STAGE_OF_WORK_ON_INSPECTION_NAME, stage_of_work_on_inspection_name);
            inspectionValue.put(AppConstant.OBSERVATION, observation);
            inspectionValue.put(AppConstant.INSPECTION_REMARK, inspection_remark);
           inspectionValue.put(AppConstant.AE_USERNAME, ae_username);

            long rowUpdated = LoginScreen.db.update(DBHelper.INSPECTION_PENDING, inspectionValue, "work_id  = ? AND delete_flag = ? and inspection_id = ?", new String[]{work_id,"0", String.valueOf(inspectionID) });

            if (rowUpdated != -1) {
                Toast.makeText(AddInspectionReportScreen.this, "New Inspection added", Toast.LENGTH_SHORT).show();
                Dashboard.getPendingCount();
               finish();
            } else {
                Toast.makeText(AddInspectionReportScreen.this, "Something wrong", Toast.LENGTH_SHORT).show();
            }

           // db.rawQuery("UPDATE "+DBHelper.INSPECTION_PENDING+" SET (stage_of_work_on_inspection, stage_of_work_on_inspection_name, observation,inspection_remark) = ('"+stage_of_work_on_inspection+"', '"+stage_of_work_on_inspection_name+"', '"+observation+"', '"+inspection_remark+"')  WHERE delete_flag=0 and inspection_id = "+inspectionID+" and work_id ="+work_id, null);
        } else {
            try {

                dataset.put(AppConstant.STAGE_OF_WORK_ON_INSPECTION, stage_of_work_on_inspection);
                dataset.put(AppConstant.OBSERVATION, observation);
                dataset.put(AppConstant.INSPECTION_REMARK, inspection_remark);
                dataset.put(AppConstant.AE_USERNAME, ae_username);
                if(prefManager.getLevels().equalsIgnoreCase("S")) {
                    dataset.put(AppConstant.DISTRICT_CODE, prefManager.getDistrictCode());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        String authKey = dataset.toString();
                        int maxLogSize = 2000;
                        for(int i = 0; i <= authKey.length() / maxLogSize; i++) {
                            int start = i * maxLogSize;
                            int end = (i+1) * maxLogSize;
                            end = end > authKey.length() ? authKey.length() : end;
                            Log.v("to_send+_plain", authKey.substring(start, end));
                     }

        String authKey1 = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), dataset.toString());

                        for(int i = 0; i <= authKey1.length() / maxLogSize; i++) {
                            int start = i * maxLogSize;
                            int end = (i+1) * maxLogSize;
                            end = end > authKey.length() ? authKey1.length() : end;
                            Log.v("to_send_encryt", authKey1.substring(start, end));
                     }
            sync_data();
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

    //Method for update single view based on email or mobile type
    public View updateView(final Activity activity, final LinearLayout emailOrMobileLayout, final String values, final String type) {
        final View hiddenInfo = activity.getLayoutInflater().inflate(R.layout.image_with_description, emailOrMobileLayout, false);
        final ImageView imageView_close = (ImageView) hiddenInfo.findViewById(R.id.imageView_close);
        imageView = (ImageView) hiddenInfo.findViewById(R.id.image_view);
        image_view_preview = (ImageView) hiddenInfo.findViewById(R.id.image_view_preview);
        final EditText myEditTextView = (EditText) hiddenInfo.findViewById(R.id.description);


        Typeface typeFace = Typeface.createFromAsset(activity.getAssets(), "fonts/Avenir-Roman.ttf");

        myEditTextView.setSelection(0);
        if ("Mobile".equalsIgnoreCase(type)) {

            if (!values.isEmpty()) {

                if (values.length() > 0 && values.contains("-")) {
                    String[] mobile = values.split("-");
                    if (mobile.length == 2) {
                        myEditTextView.setText(values.split("-")[1]);
                        int countryCode = Integer.parseInt(values.split("-")[0]);

                    }
                } /*else {
                    myEditTextView.setText(values);
                }*/
            }
        }
        if ("localImage".equalsIgnoreCase(type)) {
            int i = Integer.parseInt(values);
            if (!values.isEmpty()) {
                myEditTextView.setText(imagelistvalues.get(i).getDescription());

                image_view_preview.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(imagelistvalues.get(i).getImage());

            }
        }
        imageView_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    imageView.setVisibility(View.VISIBLE);
                    if (viewArrayList.size() != 1) {
                        ((LinearLayout) hiddenInfo.getParent()).removeView(hiddenInfo);
                        viewArrayList.remove(hiddenInfo);
                    }

                } catch (IndexOutOfBoundsException a) {
                    a.printStackTrace();
                }
            }
        });
        image_view_preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLatLong();

            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLatLong();
            }
        });
        emailOrMobileLayout.addView(hiddenInfo);

        View vv = emailOrMobileLayout.getChildAt(viewArrayList.size());
        EditText myEditTextView1 = (EditText) vv.findViewById(R.id.description);
        //myEditTextView1.setSelection(myEditTextView1.length());
        myEditTextView1.requestFocus();
        viewArrayList.add(hiddenInfo);
        return hiddenInfo;
    }

    private void getLatLong() {
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();


        // permission was granted, yay! Do the
        // location-related task you need to do.
        if (ContextCompat.checkSelfPermission(AddInspectionReportScreen.this,
                ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            //Request location updates:
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

        }

        if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(AddInspectionReportScreen.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddInspectionReportScreen.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{CAMERA, ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            } else {
                if (ActivityCompat.checkSelfPermission(AddInspectionReportScreen.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddInspectionReportScreen.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddInspectionReportScreen.this, new String[]{ACCESS_FINE_LOCATION}, 1);

                }
            }
            if (MyLocationListener.latitude > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (CameraUtils.checkPermissions(AddInspectionReportScreen.this)) {
                        captureImage();
                    } else {
                        requestCameraPermission(MEDIA_TYPE_IMAGE);
                    }
//                            checkPermissionForCamera();
                } else {
                    captureImage();
                }
            } else {
                Utils.showAlert(AddInspectionReportScreen.this, "Satellite communication not available to get GPS Co-ordination Please Capture Photo in Open Area..");
            }
        } else {
            Utils.showAlert(AddInspectionReportScreen.this, "GPS is not turned on...");
        }
    }

    private void captureImage() {
        if (CroperinoFileUtil.verifyStoragePermissions(AddInspectionReportScreen.this)) {
            prepareCamera();
        }
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            File file = CameraUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (file != null) {
                imageStoragePath = file.getAbsolutePath();
            }

            Uri fileUri = CameraUtils.getOutputMediaFileUri(this, file);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            // start the image capture Intent
            startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
        }*/
    }
    private void prepareCamera() {
        Croperino.prepareCamera(AddInspectionReportScreen.this);
    }


    private void requestCameraPermission(final int type) {
        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {

                            if (type == MEDIA_TYPE_IMAGE) {
                                // capture picture
                                captureImage();
                            } else {
//                                captureVideo();
                            }

                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            showPermissionsAlert();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }


    private void showPermissionsAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissions required!")
                .setMessage("Camera needs few permissions to work properly. Grant them in settings.")
                .setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CameraUtils.openSettings(AddInspectionReportScreen.this);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    public void previewCapturedImage() {
        try {
            // hide video preview


            Bitmap bitmap = CameraUtils.optimizeBitmap(BITMAP_SAMPLE_SIZE, imageStoragePath);
            image_view_preview.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            ExifInterface ei = null;
            try {
                ei = new ExifInterface(imageStoragePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            Bitmap rotatedBitmap = null;
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
            imageView.setImageBitmap(rotatedBitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    image_view_preview.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(photo);
                }else {
                // Refreshing the gallery
                CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);

                // successfully captured the image
                // display it in image view
                previewCapturedImage();}
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        } else if (requestCode == CAMERA_CAPTURE_VIDEO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Refreshing the gallery
                CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);

                // video successfully recorded
                // preview the recorded video
//                previewVideo();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled recording
                Toast.makeText(getApplicationContext(),
                        "User cancelled video recording", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to record video
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to record video", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CroperinoConfig.REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri i = Uri.fromFile(CroperinoFileUtil.getTempFile());
                    // Capture Image
                    image_view_preview.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageURI(i);
                    File fdelete = new File(i.getPath());
                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            System.out.println("file Deleted :" + i.getPath());
                        } else {
                            System.out.println("file not Deleted :" + i.getPath());
                        }
                    }

                    //Start Crop Image
//                    Croperino.runCropImage(CroperinoFileUtil.getTempFile(), AddInspectionReportScreen.this, true, 1, 1, R.color.gray, R.color.gray_variant);
                }
                break;
            case CroperinoConfig.REQUEST_PICK_FILE:
                if (resultCode == Activity.RESULT_OK) {
                    CroperinoFileUtil.newGalleryFile(data, AddInspectionReportScreen.this);
                    Croperino.runCropImage(CroperinoFileUtil.getTempFile(), AddInspectionReportScreen.this, true, 1, 1, R.color.gray, R.color.gray_variant);
                }
                break;
            case CroperinoConfig.REQUEST_CROP_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Uri i = Uri.fromFile(CroperinoFileUtil.getTempFile());
                    image_view_preview.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageURI(i);
                    File fdelete = new File(i.getPath());
                    if (fdelete.exists()) {
                        if (fdelete.delete()) {
                            System.out.println("file Deleted :" + i.getPath());
                        } else {
                            System.out.println("file not Deleted :" + i.getPath());
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CroperinoFileUtil.REQUEST_CAMERA) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.CAMERA)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        prepareCamera();
                    }
                }
            }
        } else if (requestCode == CroperinoFileUtil.REQUEST_EXTERNAL_STORAGE) {
            boolean wasReadGranted = false;
            boolean wasWriteGranted = false;

            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        wasReadGranted = true;
                    }
                }
                if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        wasWriteGranted = true;
                    }
                }
            }

            if (wasReadGranted && wasWriteGranted) {
                prepareChooser();
            }
        }
    }
    private void prepareChooser() {
        Croperino.prepareChooser(AddInspectionReportScreen.this, "Capture photo...", ContextCompat.getColor(AddInspectionReportScreen.this, android.R.color.background_dark));
    }

    public void onBackPress() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);

    }

    @Override
    public void OnError(VolleyError volleyError) {

    }

    public static Cursor getRawEvents(String sql, String string) {
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }

/*
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    captureImage();
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
//                        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }
*/

    public void sync_data() {
        try {
            new ApiService(this).makeJSONObjectRequest("save_data", Api.Method.POST, UrlGenerator.getInspectionServicesListUrl(), dataTobeSavedJsonParams(), "not cache", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject dataTobeSavedJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), dataset.toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("saving", "" + authKey);
        return dataSet;
    }

    public void getInspectionList_blockwise() {
        try {
            new ApiService(this).makeJSONObjectRequest("InspectionListBlockWise", Api.Method.POST, UrlGenerator.getInspectionServicesListUrl(), InspectionListBlockwiseJsonParams(), "not cache", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getInspectionList_Images_blockwise() {
        try {
            new ApiService(this).makeJSONObjectRequest("InspectionListBlockWise_Images", Api.Method.POST, UrlGenerator.getInspectionServicesListUrl(), InspectionListImageJsonParams(), "not cache", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getAction_ForInspection() {
        try {
            new ApiService(this).makeJSONObjectRequest("InspectionListBlockWise_Action", Api.Method.POST, UrlGenerator.getInspectionServicesListUrl(), InspectionListActionJsonParams(), "not cache", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject InspectionListBlockwiseJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), Utils.InspectionListblockWise(this).toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("InspectionList", "" + authKey);
        return dataSet;
    }

    public JSONObject InspectionListImageJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), Utils.InspectionList_Image(this).toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("InspectionList_Image", "" + authKey);
        return dataSet;
    }

    public JSONObject InspectionListActionJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), Utils.InspectionList_Action(this).toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("InspectionList_Action", "" + authKey);
        return dataSet;
    }

    @Override
    public void OnMyResponse(ServerResponse serverResponse) {
        try {
            String urlType = serverResponse.getApi();
            JSONObject responseObj = serverResponse.getJsonResponse();
            if (prefManager.getLevels().equalsIgnoreCase("D") || prefManager.getLevels().equalsIgnoreCase("S") ) {
                if ("save_data".equals(urlType) && responseObj != null) {
                    String key = responseObj.getString(AppConstant.ENCODE_DATA);
                    String responseDecryptedBlockKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                    JSONObject jsonObject = new JSONObject(responseDecryptedBlockKey);
                    if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                        // loadBlockList(jsonObject.getJSONArray(AppConstant.JSON_DATA));
                        getInspectionList_blockwise();
                        getInspectionList_Images_blockwise();
                        getAction_ForInspection();
                        Utils.showAlert(AddInspectionReportScreen.this, "Saved");
                        finish();
                    }
                    Log.d("saved_response", "" + responseDecryptedBlockKey);
                }

            }
            if ("InspectionListBlockWise".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String responseDecryptedKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                JSONObject jsonObject = new JSONObject(responseDecryptedKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                    Insert_inspectionList(jsonObject.getJSONArray(AppConstant.JSON_DATA));
                } else if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("NO_RECORD")) {
                    Utils.showAlert(this, "No Record Found");
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
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                Utils.showAlert(this, "No Record Found!");
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
            updatedJsonArray = new JSONArray();
            updatedJsonArray = jsonArray;
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
                Utils.showAlert(this, "No Record Found");
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
                Utils.showAlert(this, "No Record Found!");
            }

        } catch (JSONException j) {
            j.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException a) {
            a.printStackTrace();
        }

    }
}
