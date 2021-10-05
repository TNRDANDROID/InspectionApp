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
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
 * Created by NIC on 21-02-2019.
 */

public class ViewInspectionInActionScreen extends AppCompatActivity implements View.OnClickListener, Api.ServerResponseListener {
    private ScrollView scrollView;
    private MyCustomTextView action_tv;
    private List<View> viewArrayList = new ArrayList<>();

    private Context context;

    final int CAMERA_REQUEST = 1888;

    ImageView imageView;
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 2500;
    private static final int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    private static final int PERMISSION_REQUEST_CODE = 200;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;


    // key to store image path in savedInstance state
    public static final String KEY_IMAGE_STORAGE_PATH = "image_path";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // Bitmap sampling size
    public static final int BITMAP_SAMPLE_SIZE = 8;
    LocationManager mlocManager = null;
    LocationListener mlocListener;

    static int actionID = 0;
    static String inspection_id;
    private List<BlockListValue> imagelistvalues = new ArrayList<>();


    private static String imageStoragePath;
    private ImageView back_img, image_view_preview,home_img;
    private MyCustomTextView district_tv, scheme_name_tv, block_name_tv, block_user_tv, village_name_tv, fin_year_tv, take_photo, title_tv,submit;
    private MyCustomTextView projectName, amountTv, levelTv, inspected_date, remark, observation;
    private LinearLayout village_layout, block_layout;
    private RecyclerView imageRecyclerView, inspectionListRecyclerView;
    PrefManager prefManager;
    EditText remark_action_tv;


    String offlatTextValue, offlanTextValue;
    String work_id;
    EditText remarkTv;
    static JSONObject dataset;
    private JSONArray updatedJsonArray;
    private boolean imagebooleanAction;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_inspection_in_action_screen_with_search);
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
        title_tv = (MyCustomTextView) findViewById(R.id.title_tv);

        projectName = (MyCustomTextView) findViewById(R.id.project_title_tv);
        amountTv = (MyCustomTextView) findViewById(R.id.amount_tv);
        levelTv = (MyCustomTextView) findViewById(R.id.level_tv);
        take_photo = (MyCustomTextView) findViewById(R.id.take_photo);
        submit = (MyCustomTextView) findViewById(R.id.submit);
        inspected_date = (MyCustomTextView) findViewById(R.id.action_inspected_date);
        remark = (MyCustomTextView) findViewById(R.id.action_remark);
        observation = (MyCustomTextView) findViewById(R.id.action_observation);
        remark_action_tv = (EditText) findViewById(R.id.remark_action_tv);
        remark_action_tv.setScroller(new Scroller(this));
        remark_action_tv.setVerticalScrollBarEnabled(true);
        remark_action_tv.setMovementMethod(new ScrollingMovementMethod());

        scrollView = (ScrollView) findViewById(R.id.scroll_view);
//        action_tv = (MyCustomTextView) findViewById(R.id.action_tv);
        back_img = (ImageView) findViewById(R.id.back_img);
        home_img = (ImageView) findViewById(R.id.home_img);

        district_tv.setText(prefManager.getDistrictName());
        scheme_name_tv.setText(prefManager.getSchemeName());
        block_name_tv.setText(prefManager.getBlockName());
        fin_year_tv.setText(prefManager.getFinancialyearName());

        projectName.setText(prefManager.getKeyActionProjectName());
        amountTv.setText(prefManager.getKeyActionAmount());
        levelTv.setText(prefManager.getKeyActionStageLevel());
        inspected_date.setText(getIntent().getStringExtra(AppConstant.DATE_OF_INSPECTION));
        remark.setText(getIntent().getStringExtra(AppConstant.INSPECTION_REMARK));
        observation.setText(getIntent().getStringExtra(AppConstant.OBSERVATION));
        title_tv.setText("Record Action");
        back_img.setOnClickListener(this);
        home_img.setOnClickListener(this);
        take_photo.setOnClickListener(this);
        submit.setOnClickListener(this);
        new CroperinoConfig("IMG_" + System.currentTimeMillis() + ".jpg", "/MikeLau/Pictures", "/sdcard/MikeLau/Pictures");
        CroperinoFileUtil.setupDirectory(ViewInspectionInActionScreen.this);

        if (prefManager.getLevels().equalsIgnoreCase("B")) {
            village_layout.setVisibility(View.VISIBLE);
            village_name_tv.setText(prefManager.getVillageListPvName());
            block_layout.setVisibility(View.VISIBLE);
            block_user_tv.setText(prefManager.getBlockName());
        }
        imagebooleanAction = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit:
                if (imagebooleanAction) {
                    if (!remark_action_tv.getText().toString().isEmpty()) {
                        submit();
                    } else {
                        Utils.showAlert(this, "Enter Remark");
                    }
                } else {
                    Utils.showAlert(this, "Take Photo");
                }

                break;
            case R.id.back_img:
                onBackPress();
                break;

            case R.id.home_img:
                onBackPress();
                break;

            case R.id.take_photo:
                imageWithDescription(take_photo, "mobile", scrollView);
                break;
        }
    }


    public void imageWithDescription(final MyCustomTextView action_tv, final String type, final ScrollView scrollView) {
        imagebooleanAction = true;
        dataset = new JSONObject();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        work_id = getIntent().getStringExtra(AppConstant.WORK_ID);
        inspection_id = getIntent().getStringExtra(AppConstant.INSPECTION_ID);

        String date_of_action = sdf.format(new Date());
        String action_remark = remark_action_tv.getText().toString();

        if (!Utils.isOnline()) {
            ContentValues actionValue = new ContentValues();
            actionValue.put(AppConstant.WORK_ID, work_id);
            actionValue.put(AppConstant.INSPECTION_ID, inspection_id);
            actionValue.put(AppConstant.DATE_OF_ACTION, date_of_action);
            actionValue.put(AppConstant.ACTION_REMARK, action_remark);
            actionValue.put(AppConstant.DATE_OF_ACTION, date_of_action);
            actionValue.put(AppConstant.ACTION_TAKEN_OFFICER_DESIGNATION, prefManager.getInspectedOfficerDesignation());
            actionValue.put(AppConstant.ACTION_TAKEN_OFFICER,prefManager.getInspectedOfficerName());
            actionValue.put(AppConstant.DELETE_FLAG, 0);

            long rowInserted = LoginScreen.db.insert(DBHelper.INSPECTION_ACTION, null, actionValue);
            Log.d("rowInserted", String.valueOf(rowInserted));
        } else {
            try {
                dataset.put(AppConstant.KEY_SERVICE_ID, AppConstant.KEY_HIGH_VALUE_PROJECT_ACTION_SAVE);
                dataset.put(AppConstant.WORK_ID, work_id);
                dataset.put(AppConstant.INSPECTION_ID, inspection_id);
                dataset.put(AppConstant.CREATED_IMEI_NO, prefManager.getIMEI());
                dataset.put(AppConstant.DATE_OF_ACTION, date_of_action);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }


        if (!Utils.isOnline()) {
            Cursor action_Cursor = getRawEvents("SELECT MAX(id) FROM " + DBHelper.INSPECTION_ACTION, null);
            Log.d("cursor_count", String.valueOf(action_Cursor.getCount()));
            if (action_Cursor.getCount() > 0) {
                if (action_Cursor.moveToFirst()) {
                    do {
                        actionID = action_Cursor.getInt(0);
                        Log.d("actionID", "" + actionID);
                    } while (action_Cursor.moveToNext());
                }
            }
        }
        String imagelist_sql = "select * from " + DBHelper.CAPTURED_PHOTO + " where inspection_id=" + inspection_id+" and action_id is null ";
        Log.d("sql", imagelist_sql);
        Cursor imagelist = getRawEvents(imagelist_sql, null);

        if(Utils.isOnline()) {
            db.delete(DBHelper.IMAGE_GROUP_ID_ONLINE,null,null);
        }

        if (imagelist.getCount() > 0) {
           JSONArray group = new JSONArray();
            if (imagelist.moveToFirst()) {
                int i = 0;
                do {
                    String image_id = imagelist.getString(imagelist.getColumnIndexOrThrow("id"));
                   group.put(image_id);
                  //  friendsNames.add(image_id);
                    i++;
                } while (imagelist.moveToNext());
            }
            ContentValues groupedValue = new ContentValues();
            groupedValue.put("action_id",actionID);
            groupedValue.put("grouping", String.valueOf(group));
            Log.d("grouping",group.toString());
            if(Utils.isOnline()) {
                LoginScreen.db.insert(DBHelper.IMAGE_GROUP_ID_ONLINE,null,groupedValue);
            }else {
                LoginScreen.db.insert(DBHelper.IMAGE_GROUP_ID_OFFLINE,null,groupedValue);
            }

        }

        String list_sql = "select * from " + DBHelper.IMAGE_GROUP_ID_ONLINE;
        Log.d("sql", imagelist_sql);
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

        final int finalActionID = actionID;
        done.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int image_group_id = 1;
                JSONArray imageJson = new JSONArray();
                Cursor image_Cursor;

                if(Utils.isOnline()) {
                    image_Cursor = getRawEvents("SELECT MAX(id) FROM " + DBHelper.IMAGE_GROUP_ID_ONLINE, null);
                }else {
                    image_Cursor = getRawEvents("SELECT MAX(id) FROM " + DBHelper.IMAGE_GROUP_ID_OFFLINE, null);
                }


                Log.d("cursor_count", String.valueOf(image_Cursor.getCount()));
                if (image_Cursor.getCount() > 0) {
                    if (image_Cursor.moveToFirst()) {
                        do {
                            image_group_id = image_Cursor.getInt(0);
                            Log.d("inspectionID", "" + image_group_id);
                        } while (image_Cursor.moveToNext());
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
                            Utils.showAlert(ViewInspectionInActionScreen.this, "Atleast Capture one Photo");
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

                        imageValue.put(AppConstant.INSPECTION_ID, inspection_id);
                        imageValue.put(AppConstant.WORK_ID, work_id);
                        imageValue.put(AppConstant.LATITUDE, offlatTextValue);
                        imageValue.put(AppConstant.LONGITUDE, offlanTextValue);
                        imageValue.put(AppConstant.IMAGE, image_str.trim());
                        imageValue.put(AppConstant.DESCRIPTION, description);
                        imageValue.put("action_id", finalActionID);
                        imageValue.put("image_group_id", image_group_id);
                        imageValue.put("pending_flag", 1);
                        imageValue.put("level", "B");

                        if (!Utils.isOnline()) {

                            long rowInserted = LoginScreen.db.insert(DBHelper.CAPTURED_PHOTO, null, imageValue);

//                            if (rowInserted != -1) {
//                                Toast.makeText(ViewInspectionInActionScreen.this, "New Action added", Toast.LENGTH_SHORT).show();
//                                Dashboard.getPendingCount();
//                                finish();
//                            } else {
//                                Toast.makeText(ViewInspectionInActionScreen.this, "Something wrong", Toast.LENGTH_SHORT).show();
//                            }
                        } else {
                            imageArray.put(work_id);
                            imageArray.put(image_group_id);
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

                        Log.d("post_dataset_action", dataset.toString());
                     // String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), dataset.toString());
                      String authKey = dataset.toString();
                        int maxLogSize = 4000;
                        for(int i = 0; i <= authKey.length() / maxLogSize; i++) {
                            int start = i * maxLogSize;
                           int end = (i+1) * maxLogSize;
                            end = end > authKey.length() ? authKey.length() : end;
                            Log.v("to_send", authKey.substring(start, end));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
//                    if (Utils.isOnline()) {
//                        sync_data();
//                    }
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
                if (imageView.getDrawable() != null) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    updateView(ViewInspectionInActionScreen.this, mobileNumberLayout, "", type);
                } else {
                    Utils.showAlert(ViewInspectionInActionScreen.this, "Capture Image!");
                }
            }
        });
        if (!values.isEmpty()) {

            Cursor imageList = getRawEvents("SELECT * FROM " + DBHelper.LOCAL_IMAGE +" WHERE level='B' and work_id="+work_id, null);

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

                        //  byte[] image =  imageListPreview.getBlob(imageListPreview.getColumnIndexOrThrow(AppConstant.IMAGE));


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
        String action_remark = remark_action_tv.getText().toString();

        if (!Utils.isOnline()) {
            ContentValues actionValue = new ContentValues();
            actionValue.put(AppConstant.WORK_ID, work_id);
            actionValue.put(AppConstant.INSPECTION_ID, inspection_id);
            actionValue.put(AppConstant.ACTION_REMARK, action_remark);


            long rowUpdated = LoginScreen.db.update(DBHelper.INSPECTION_ACTION, actionValue, "work_id  = ? AND delete_flag = ? and id = ? and inspection_id =?", new String[]{work_id,"0", String.valueOf(actionID),inspection_id });

            if (rowUpdated != -1) {
                Toast.makeText(ViewInspectionInActionScreen.this, "New Action added", Toast.LENGTH_SHORT).show();
                Dashboard.getPendingCount();
                finish();
            } else {
                Toast.makeText(ViewInspectionInActionScreen.this, "Something wrong", Toast.LENGTH_SHORT).show();
            }

            // db.rawQuery("UPDATE "+DBHelper.INSPECTION_PENDING+" SET (stage_of_work_on_inspection, stage_of_work_on_inspection_name, observation,inspection_remark) = ('"+stage_of_work_on_inspection+"', '"+stage_of_work_on_inspection_name+"', '"+observation+"', '"+inspection_remark+"')  WHERE delete_flag=0 and inspection_id = "+inspectionID+" and work_id ="+work_id, null);
        } else {
            try {
                dataset.put(AppConstant.ACTION_REMARK, action_remark);
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
            Log.v("to_send_plain", authKey.substring(start, end));
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
        if (ContextCompat.checkSelfPermission(ViewInspectionInActionScreen.this,
                ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            //Request location updates:
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

        }

        if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(ViewInspectionInActionScreen.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ViewInspectionInActionScreen.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    requestPermissions(new String[]{CAMERA, ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            } else {
                if (ActivityCompat.checkSelfPermission(ViewInspectionInActionScreen.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ViewInspectionInActionScreen.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ViewInspectionInActionScreen.this, new String[]{ACCESS_FINE_LOCATION}, 1);

                }
            }
            if (MyLocationListener.latitude > 0) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (CameraUtils.checkPermissions(ViewInspectionInActionScreen.this)) {
                        captureImage();
                    } else {
                        requestCameraPermission(MEDIA_TYPE_IMAGE);
                    }
//                            checkPermissionForCamera();
                } else {
                    captureImage();
                }
            } else {
                Utils.showAlert(ViewInspectionInActionScreen.this, "Satellite communication not available to get GPS Co-ordination Please Capture Photo in Open Area..");
            }
        } else {
            Utils.showAlert(ViewInspectionInActionScreen.this, "GPS is not turned on...");
        }
    }

    private void captureImage() {
        if (CroperinoFileUtil.verifyStoragePermissions(ViewInspectionInActionScreen.this)) {
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
        Croperino.prepareCamera(ViewInspectionInActionScreen.this);
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
                        CameraUtils.openSettings(ViewInspectionInActionScreen.this);
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
                    CroperinoFileUtil.newGalleryFile(data, ViewInspectionInActionScreen.this);
                    Croperino.runCropImage(CroperinoFileUtil.getTempFile(), ViewInspectionInActionScreen.this, true, 1, 1, R.color.gray, R.color.gray_variant);
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
        Croperino.prepareChooser(ViewInspectionInActionScreen.this, "Capture photo...", ContextCompat.getColor(ViewInspectionInActionScreen.this, android.R.color.background_dark));
    }
    public void dashboard() {
        Intent intent = new Intent(this, Dashboard.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
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
    public void OnMyResponse(ServerResponse serverResponse) {
        try {
            String urlType = serverResponse.getApi();
            JSONObject responseObj = serverResponse.getJsonResponse();
         //   if (prefManager.getLevels().equalsIgnoreCase("D")) {
                if ("save_data".equals(urlType) && responseObj != null) {
                    String key = responseObj.getString(AppConstant.ENCODE_DATA);
                    String responseDecryptedBlockKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                    JSONObject jsonObject = new JSONObject(responseDecryptedBlockKey);
                    if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                        // loadBlockList(jsonObject.getJSONArray(AppConstant.JSON_DATA));
                        getActionImages();
                        getAction_ForInspection();
                        Utils.showAlert(this, "Saved");
                        finish();
                    }
                    Log.d("saved_response", "" + responseDecryptedBlockKey);
              // }

            }
            if ("InspectionListBlockWise_Action".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String responseDecryptedKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                JSONObject jsonObject = new JSONObject(responseDecryptedKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                    Insert_inspectionList_Action(jsonObject.getJSONArray(AppConstant.JSON_DATA));
                } else if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("NO_RECORD")) {
                    // Utils.showAlert(this, "No Record Found");
                    Log.d("responseInspect_Action",jsonObject.getString("MESSAGE"));
                }
                Log.d("responseInspect_Action", "" + jsonObject.getJSONArray(AppConstant.JSON_DATA));

            }

            if ("ActionImages".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String responseDecryptedKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                JSONObject jsonObject = new JSONObject(responseDecryptedKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                    Insert_ActionList_Images(jsonObject.getJSONArray(AppConstant.JSON_DATA));
                    String authKey = String.valueOf(jsonObject.getJSONArray(AppConstant.JSON_DATA));
                    int maxLogSize = 3000;
//                    for(int i = 0; i <= authKey.length() / maxLogSize; i++) {
//                        int start = i * maxLogSize;
//                        int end = (i+1) * maxLogSize;
//                        end = end > authKey.length() ? authKey.length() : end;
//                        Log.v("imagesofAction", authKey.substring(start, end));
//                    }
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


    @Override
    public void OnError(VolleyError volleyError) {

    }

    public static Cursor getRawEvents(String sql, String string) {
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }

/*
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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


    public void getAction_ForInspection() {
        try {
            new ApiService(this).makeJSONObjectRequest("InspectionListBlockWise_Action", Api.Method.POST, UrlGenerator.getInspectionServicesListUrl(), InspectionListActionJsonParams(), "not cache", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void getActionImages() {
        try {
            new ApiService(this).makeJSONObjectRequest("ActionImages", Api.Method.POST, UrlGenerator.getInspectionServicesListUrl(), ActionImagesJsonParams(), "not cache", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public JSONObject InspectionListActionJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), Utils.InspectionList_Action(this).toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("InspectionList_Action", "" + authKey);
        return dataSet;
    }

    public JSONObject ActionImagesJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), Utils.ActionImages(this).toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("Action_Images", "" + authKey);
        return dataSet;
    }

    private void Insert_inspectionList_Action(JSONArray jsonArray) {
        try {
            db.execSQL(String.format("DELETE FROM " + DBHelper.INSPECTION_ACTION + " WHERE delete_flag=1;", null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
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

    private void Insert_ActionList_Images(JSONArray jsonArray) {
        try {
            // db.delete(DBHelper.CAPTURED_PHOTO, null, null);
            db.execSQL(String.format("DELETE FROM " + DBHelper.ACTION_PHOTO, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
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

            } else {
                Utils.showAlert(this, "No Record Found");
            }

        } catch (JSONException j) {
            j.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException a) {
            a.printStackTrace();
        }
    }

}
