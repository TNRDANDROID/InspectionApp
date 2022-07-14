package com.nic.Inspection.Activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.android.volley.VolleyError;
import com.nic.Inspection.Adapter.ProjectListAdapter;
import com.nic.Inspection.DataBase.DBHelper;
import com.nic.Inspection.Model.BlockListValue;
import com.nic.Inspection.R;
import com.nic.Inspection.Support.MyCustomTextView;
import com.nic.Inspection.Utils.Utils;
import com.nic.Inspection.api.Api;
import com.nic.Inspection.api.ServerResponse;
import com.nic.Inspection.constant.AppConstant;
import com.nic.Inspection.session.PrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.nic.Inspection.Activity.LoginScreen.db;

/**
 * Created by AchanthiSundar on 04-01-2019.
 */

public class ProjectListScreen extends AppCompatActivity implements View.OnClickListener, Api.ServerResponseListener, ProjectListAdapter.ProjectsAdapterListener {
    private RecyclerView recyclerView;
    private ArrayList<BlockListValue> projectListValues;
    private ProjectListAdapter mAdapter;
    private ImageView back_img,hide_show;
    private LinearLayout village_layout,block_user_layout,scheme_block_tv;
    private NestedScrollView scrollView;
    private SearchView searchView;
    private MyCustomTextView district_tv, scheme_name_tv,block_user_tv, block_name_tv, fin_year_tv, list_count, not_found_tv, village_name_tv,title_tv;
    PrefManager prefManager;
    private JSONArray updatedJsonArray;
    boolean flag=true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_list_activity);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        intializeUI();
        //  recycle();

    }

    public void intializeUI() {
        prefManager = new PrefManager(this);
        block_user_layout = (LinearLayout)findViewById(R.id.block_user_layout);
        block_user_tv = (MyCustomTextView)findViewById(R.id.block_user_tv);
        village_layout = (LinearLayout) findViewById(R.id.village_layout);
        district_tv = (MyCustomTextView) findViewById(R.id.district_tv);
        scheme_block_tv = (LinearLayout) findViewById(R.id.scheme_block_tv);
        scheme_name_tv = (MyCustomTextView) findViewById(R.id.scheme_name_tv);
        block_name_tv = (MyCustomTextView) findViewById(R.id.block_name_tv);
        village_name_tv = (MyCustomTextView) findViewById(R.id.village_name_tv);
        fin_year_tv = (MyCustomTextView) findViewById(R.id.fin_year_tv);
        recyclerView = (RecyclerView) findViewById(R.id.project_list);
        not_found_tv = (MyCustomTextView) findViewById(R.id.not_found_tv);
        list_count = (MyCustomTextView) findViewById(R.id.count_list);
        title_tv = (MyCustomTextView) findViewById(R.id.title_tv);
        projectListValues = new ArrayList<>();
        back_img = (ImageView) findViewById(R.id.backimg);
        hide_show = (ImageView) findViewById(R.id.hide_show);
        back_img.setOnClickListener(this);
        hide_show.setOnClickListener(this);

        scheme_block_tv.setVisibility(View.GONE);
        district_tv.setText(prefManager.getDistrictName());
        scheme_name_tv.setText(prefManager.getSchemeName());
        block_name_tv.setText(prefManager.getBlockName());
        fin_year_tv.setText(prefManager.getFinancialyearName());

        if (prefManager.getLevels().equalsIgnoreCase("B")) {
            block_user_layout.setVisibility(View.VISIBLE);
            block_user_tv.setText(prefManager.getBlockName());
            village_layout.setVisibility(View.VISIBLE);
            village_name_tv.setText(prefManager.getVillageListPvName());
            title_tv.setText("View U/SRI Works");
        }else{
            title_tv.setText("Work List");
        }


        mAdapter = new ProjectListAdapter(this, projectListValues, this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        retrieve();

        recyclerView.setFocusable(false);
    }

    private void retrieve() {
        projectListValues.clear();
        String selectedDistrict = getIntent().getStringExtra(AppConstant.DISTRICT_CODE);
        String selectedBlock = getIntent().getStringExtra(AppConstant.BLOCK_CODE);
        String selectedVillage = getIntent().getStringExtra(AppConstant.PV_CODE);
        String selectedScheme = getIntent().getStringExtra(AppConstant.SCHEME_SEQUENTIAL_ID);
        String high_value = getIntent().getStringExtra(AppConstant.IS_HIGH_VALUE_PROJECT);

        String condition = "";

        if (selectedBlock != null || selectedVillage != null || selectedScheme != null || high_value != null) {
            condition += " where";

            if (high_value != null) {
                condition += " a.is_high_value = '" + high_value + "'";
            }
//            if (selectedBlock != null) {
//                if (high_value != null) {
//                    condition += " and";
//                }
//                condition += " a.bcode = " + selectedBlock;
//
//                if (selectedVillage != null) {
//                    condition += " and a.pvcode = " + selectedVillage;
//                }
//
//            }

            if (selectedDistrict != null) {
                if (high_value != null ) {
                    condition += " and";
                }
                condition += " a.dcode = " + selectedDistrict;

                if (selectedBlock != null) {
                    condition += " and a.bcode = " + selectedBlock;
                }

                if (selectedVillage != null) {
                    condition += " and a.pvcode = " + selectedVillage;
                }

            }
            if (selectedScheme != null) {
                if (high_value != null || selectedBlock != null || selectedDistrict != null) {
                    condition += " and";
                }
                condition += " a.scheme_id = " + selectedScheme;
            }
        }
        String dcode;
        if(prefManager.getLevels().equalsIgnoreCase("S")) {
            dcode = selectedDistrict;
        }
        else {
            dcode = prefManager.getDistrictCode();
        }

        String worklist_sql = "select a.bcode as bcode,a.pvcode as pvcode,a.scheme_id as scheme_id,a.work_group_id as work_group_id,a.work_type_id as work_type_id,a.work_id as work_id,a.work_name as work_name,a.as_value as as_value,a.ts_value as ts_value,a.is_high_value as is_high_value,b.work_stage_code as work_stage_code,b.work_stage_order as work_stage_order,b.work_stage_name as  work_stage_name from (select * from " + DBHelper.WORK_LIST_OPTIONAL + " WHERE dcode = " + dcode + " AND fin_year = '" + prefManager.getFinancialyearName() + "')a left join (select * from " + DBHelper.WORK_STAGE_TABLE + ")b on a.work_group_id = b.work_group_id and a.work_type_id = b.work_type_id and a.current_stage_of_work=b.work_stage_code " + condition + "  order by b.work_stage_order ";
        Log.d("sql", worklist_sql);
        Cursor worklist = getRawEvents(worklist_sql, null);

        if (worklist.getCount() > 0) {
            if (worklist.moveToFirst()) {
                do {
                    String bcode = worklist.getString(worklist.getColumnIndexOrThrow(AppConstant.BLOCK_CODE));
                    String pvcode = worklist.getString(worklist.getColumnIndexOrThrow(AppConstant.PV_CODE));
                    String scheme_id = worklist.getString(worklist.getColumnIndexOrThrow(AppConstant.SCHEME_ID));
                    String work_group_id = worklist.getString(worklist.getColumnIndexOrThrow(AppConstant.WORK_GROUP_ID));
                    String work_type_id = worklist.getString(worklist.getColumnIndexOrThrow(AppConstant.WORK_TYPE_ID));
                    String work_id = worklist.getString(worklist.getColumnIndexOrThrow(AppConstant.WORK_ID));
                    String work_name = worklist.getString(worklist.getColumnIndexOrThrow(AppConstant.WORK_NAME));
                    String as_value = worklist.getString(worklist.getColumnIndexOrThrow(AppConstant.AS_AMOUNT));
                    String is_high_value = worklist.getString(worklist.getColumnIndexOrThrow(AppConstant.IS_HIGH_VALUE_PROJECT));
                    String work_stage_code = worklist.getString(worklist.getColumnIndexOrThrow(AppConstant.WORK_STAGE_CODE));
                    String work_satge_order = worklist.getString(worklist.getColumnIndexOrThrow(AppConstant.WORK_STAGE_ORDER));
                    String work_stage_name = worklist.getString(worklist.getColumnIndexOrThrow(AppConstant.WORK_SATGE_NAME));

                    BlockListValue workListValue = new BlockListValue();
                    workListValue.setBlockCode(bcode);
                    workListValue.setPvCode(pvcode);
                    workListValue.setSchemeID(scheme_id);
                    workListValue.setWorkGroupID(work_group_id);
                    workListValue.setWorkTypeID(work_type_id);
                    workListValue.setWorkID(work_id);
                    workListValue.setWorkName(work_name);
                    workListValue.setAsAmount(as_value);
                    workListValue.setIsHighValue(is_high_value);
                    workListValue.setWorkStageCode(work_stage_code);
                    workListValue.setWorkStageOrder(work_satge_order);
                    workListValue.setWorkStageName(work_stage_name);
                    projectListValues.add(workListValue);

                } while (worklist.moveToNext());
            }
        }

        if (!(projectListValues.size() < 1)) {
            recyclerView.setAdapter(mAdapter);
            Log.d("size", String.valueOf(projectListValues.size()));
            list_count.setText(String.valueOf(projectListValues.size()));

        } else {
            list_count.setText("0");
            not_found_tv.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                mAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                mAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backimg:
                //DashboardFragment.setViolation();
                onBackPress();
                break;
            case R.id.hide_show:
                show();
                break;

        }
    }

    private void show() {
        if(flag){
            scheme_block_tv.setVisibility(View.VISIBLE);
            hide_show.setRotation(270);
            flag=false;
        }else {
            scheme_block_tv.setVisibility(View.GONE);
            hide_show.setRotation(90);
            flag=true;
        }
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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPress();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void whiteNotificationBar(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = view.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            view.setSystemUiVisibility(flags);
            getWindow().setStatusBarColor(getColor(R.color.colorPrimary));
        }
    }


    @Override
    public void OnMyResponse(ServerResponse serverResponse) {
        try {

            String urlType = serverResponse.getApi();
            JSONObject responseObj = serverResponse.getJsonResponse();


        } catch (JSONException e) {

            e.printStackTrace();
        }
    }

    private void Insert_inspectionList(JSONArray jsonArray) {
//        try {
//            db.delete(DBHelper.WORK_LIST_OPTIONAL, null, null);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        try {
            updatedJsonArray = new JSONArray();
            updatedJsonArray = jsonArray;
            if (jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String inspection_id = jsonArray.getJSONObject(i).getString(AppConstant.INSPECTION_ID);
                    String work_id = jsonArray.getJSONObject(i).getString(AppConstant.WORK_ID);
                    String stage_of_work_on_inspection = jsonArray.getJSONObject(i).getString(AppConstant.STAGE_OF_WORK_ON_INSPECTION);
                    String date_of_inspection = jsonArray.getJSONObject(i).getString(AppConstant.DATE_OF_INSPECTION);
                    String inspected_by = jsonArray.getJSONObject(i).getString(AppConstant.INSPECTED_BY);
                    String observation = jsonArray.getJSONObject(i).getString(AppConstant.OBSERVATION);
                    String inspection_remark = jsonArray.getJSONObject(i).getString(AppConstant.INSPECTION_REMARK);
                    String created_date = jsonArray.getJSONObject(i).getString(AppConstant.CREATED_DATE);
                    String created_username = jsonArray.getJSONObject(i).getString(AppConstant.CREATED_USER_NAME);
                    String created_ipaddress = jsonArray.getJSONObject(i).getString(AppConstant.CREATED_IMEI_NO);

                    ContentValues InspectValue = new ContentValues();

                    InspectValue.put(AppConstant.INSPECTION_ID,inspection_id);
                    InspectValue.put(AppConstant.WORK_ID,work_id);
                    InspectValue.put(AppConstant.STAGE_OF_WORK_ON_INSPECTION,stage_of_work_on_inspection);
                    InspectValue.put(AppConstant.DATE_OF_INSPECTION,date_of_inspection);
                    InspectValue.put(AppConstant.INSPECTED_BY,inspected_by);
                    InspectValue.put(AppConstant.OBSERVATION,observation);
                    InspectValue.put(AppConstant.INSPECTION_REMARK,inspection_remark);
                    InspectValue.put(AppConstant.CREATED_DATE,created_date);
                    InspectValue.put(AppConstant.CREATED_IMEI_NO,created_username);
                    InspectValue.put(AppConstant.CREATED_USER_NAME,created_ipaddress);

                    LoginScreen.db.insert(DBHelper.INSPECTION, null, InspectValue);
                }
            } else {
                Utils.showAlert(this, "No Record Found for Corrsponding Financial Year");
            }

        } catch (JSONException j) {
            j.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException a) {
            a.printStackTrace();
        }
    }

    @Override
    public void OnError(VolleyError volleyError) {

    }

//    @Override
//    public void setProjectList(BlockListValue projectList) {
//
//    }

    @Override
    public void addInspectionOnclick(View v, int position) {
        Log.d("pos", String.valueOf(position));
    }

    public Cursor getRawEvents(String sql, String string) {
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }

    private Cursor getRawEventWhere(String sql, String[] string) {
        Cursor cursor = LoginScreen.db.rawQuery(sql, string);
        return cursor;
    }

    public int getProjectlistSize() {
        return projectListValues.size();
    }
}
