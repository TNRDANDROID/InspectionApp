package com.nic.Inspection.Activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nic.Inspection.Adapter.ImagePreviewAdapter;
import com.nic.Inspection.DataBase.DBHelper;
import com.nic.Inspection.Model.BlockListValue;
import com.nic.Inspection.R;
import com.nic.Inspection.Support.MyCustomTextView;
import com.nic.Inspection.constant.AppConstant;
import com.nic.Inspection.session.PrefManager;

import java.util.ArrayList;
import java.util.List;

import static com.nic.Inspection.Activity.LoginScreen.db;

public class ImagePreviewActionScreen extends AppCompatActivity implements View.OnClickListener {

    private PrefManager prefManager;
    private ImagePreviewAdapter imagePreviewAdapter;
    private List<BlockListValue> imagePreviewlistvalues;
    private ImageView home;
    private Button done;
    private RecyclerView image_preview_recyclerview;
    private RelativeLayout add_action_layout;
    private ImageView back_img;
    private MyCustomTextView title_tv;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_main_layout);
        intializeUI();


    }

    public void intializeUI() {
        prefManager = new PrefManager(this);
        imagePreviewlistvalues = new ArrayList<>();
        imagePreviewAdapter = new ImagePreviewAdapter(this, imagePreviewlistvalues);
        image_preview_recyclerview = (RecyclerView) findViewById(R.id.image_preview_action_recyclerview);
        home = (ImageView) findViewById(R.id.homeimg);
//        done = (Button) findViewById(R.id.btn_save);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        image_preview_recyclerview.setLayoutManager(mLayoutManager);
        image_preview_recyclerview.setItemAnimator(new DefaultItemAnimator());
        image_preview_recyclerview.setHasFixedSize(true);
        image_preview_recyclerview.setNestedScrollingEnabled(false);
        image_preview_recyclerview.setFocusable(false);
        image_preview_recyclerview.setAdapter(imagePreviewAdapter);
        title_tv = (MyCustomTextView)findViewById(R.id.title_tv);
        back_img = (ImageView) findViewById(R.id.backimg);
        back_img.setOnClickListener(this);
        home.setOnClickListener(this);
        title_tv.setText("View Inspected Image");
        retriveImageWithDescription();
    }

    public void retriveImageWithDescription() {
        imagePreviewlistvalues.clear();
        String id = getIntent().getStringExtra(AppConstant.INSPECTION_ID);
        int inspectionId = Integer.parseInt(id);


        String image_sql = "SELECT * FROM " + DBHelper.CAPTURED_PHOTO + " WHERE inspection_id = " + inspectionId+" and action_id is null ";
        Log.d("image_sql", image_sql);
        Cursor imageListPreview = getRawEvents(image_sql, null);

        if (imageListPreview.getCount() > 0) {
            if (imageListPreview.moveToFirst()) {
                do {
                    String work_id = imageListPreview.getString(imageListPreview.getColumnIndexOrThrow(AppConstant.WORK_ID));
                    String latitude = imageListPreview.getString(imageListPreview.getColumnIndexOrThrow(AppConstant.LATITUDE));
                    String longitude = imageListPreview.getString(imageListPreview.getColumnIndexOrThrow(AppConstant.LONGITUDE));
                    String description = imageListPreview.getString(imageListPreview.getColumnIndexOrThrow(AppConstant.DESCRIPTION));

                    byte[] photo = imageListPreview.getBlob(imageListPreview.getColumnIndexOrThrow(AppConstant.IMAGE));
                    byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    //  byte[] image =  imageListPreview.getBlob(imageListPreview.getColumnIndexOrThrow(AppConstant.IMAGE));


                    BlockListValue imageValue = new BlockListValue();

                    imageValue.setWorkID(work_id);
                    imageValue.setLatitude(latitude);
                    imageValue.setLongitude(longitude);
                    imageValue.setDescription(description);
                    imageValue.setImage(decodedByte);

                    imagePreviewlistvalues.add(imageValue);

                } while (imageListPreview.moveToNext());
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backimg:
                onBackPress();
                break;
            case  R.id.homeimg:
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    public void onBackPress() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    public Cursor getRawEvents(String sql, String string) {
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }


}
