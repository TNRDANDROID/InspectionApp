package com.nic.Inspection.Activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.android.volley.VolleyError;
import com.nic.Inspection.Adapter.CommonAdapter;
import com.nic.Inspection.DataBase.DBHelper;
import com.nic.Inspection.Model.BlockListValue;
import com.nic.Inspection.R;
import com.nic.Inspection.Support.MyCustomTextView;
import com.nic.Inspection.Support.ProgressHUD;
import com.nic.Inspection.Utils.Utils;
import com.nic.Inspection.api.Api;
import com.nic.Inspection.api.ServerResponse;
import com.nic.Inspection.constant.AppConstant;
import com.nic.Inspection.session.PrefManager;

import java.util.ArrayList;
import java.util.List;

import static com.nic.Inspection.Activity.LoginScreen.db;
import static com.nic.Inspection.DataBase.DBHelper.VILLAGE_TABLE_NAME;

/**
 * Created by AchanthiSundar on 04-01-2019.
 */

public class SelectBlockSchemeScreen extends AppCompatActivity implements View.OnClickListener, Api.ServerResponseListener {

    //    private ImageView home;
    private Button done;
    private RadioGroup radioGroup;
    CheckBox all_block, all_village, all_scheme, high_value_projects, all_projects;
    private Spinner sp_block, sp_district, sp_village, sp_scheme, sp_financialYear;
    private MyCustomTextView title_tv;
    private LinearLayout block_layout, district_layout;
    private PrefManager prefManager;
    private List<BlockListValue> Block = new ArrayList<>();
    private List<BlockListValue> District = new ArrayList<>();
    private List<BlockListValue> Village = new ArrayList<>();
    private List<BlockListValue> Scheme = new ArrayList<>();
    private List<BlockListValue> FinYearList = new ArrayList<>();
    private ProgressHUD progressHUD;
    private ImageView back_img, homeimg;

    String pref_Block, pref_district, pref_Village, pref_Scheme, pref_finYear;
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_block_scheme);
        intializeUI();
        checkdata_offline();
//        if (Utils.isOnline()) {
//            db.delete(DBHelper.WORK_LIST_OPTIONAL, null, null);
//            db.delete(DBHelper.INSPECTION, null, null);
//            db.delete(DBHelper.INSPECTION_ACTION, null, null);
//            db.delete(DBHelper.CAPTURED_PHOTO, null, null);
//        } else {
//            //   Utils.showAlert(this, getResources().getString(R.string.no_internet));
//        }


    }

    public void intializeUI() {
        prefManager = new PrefManager(this);
        homeimg = (ImageView) findViewById(R.id.homeimg);
        done = (Button) findViewById(R.id.btn_save);
        high_value_projects = (CheckBox) findViewById(R.id.high_value_projects);
        all_projects = (CheckBox) findViewById(R.id.all_projects);
        sp_block = (Spinner) findViewById(R.id.block);
        sp_district = (Spinner) findViewById(R.id.district);
        sp_village = (Spinner) findViewById(R.id.village);
        sp_scheme = (Spinner) findViewById(R.id.scheme);
        sp_financialYear = (Spinner) findViewById(R.id.financialYear);
        all_block = (CheckBox) findViewById(R.id.all_block);
        all_village = (CheckBox) findViewById(R.id.all_village);
        all_scheme = (CheckBox) findViewById(R.id.all_scheme);
        block_layout = (LinearLayout) findViewById(R.id.block_layout);
        district_layout = (LinearLayout) findViewById(R.id.District_layout);
        back_img = (ImageView) findViewById(R.id.backimg);
        title_tv = (MyCustomTextView) findViewById(R.id.title_tv);
        back_img.setOnClickListener(this);

        if (prefManager.getLevels().equalsIgnoreCase("S")) {
            district_layout.setVisibility(View.VISIBLE);
        } else {
            district_layout.setVisibility(View.GONE);
        }
        if (prefManager.getLevels().equalsIgnoreCase("B")) {
            title_tv.setText("Select a Work For Action");
            block_layout.setVisibility(View.GONE);
        } else {
            title_tv.setText("Select a Work For Inspection");

            block_layout.setVisibility(View.VISIBLE);
        }

        done.setOnClickListener(this);
        homeimg.setOnClickListener(this);

        high_value_projects.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    all_projects.setChecked(false);
                } else {
                    all_projects.setChecked(true);
                }

            }
        });
        all_projects.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    high_value_projects.setChecked(false);
                } else {
                    high_value_projects.setChecked(true);
                }

            }
        });

        sp_district.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {

//                    prefManager.setBlockName("All");
//                    loadOfflineVillgeListDBValues();
                } else {

                    pref_district = District.get(position).getDistrictName();
                    prefManager.setDistrictName(pref_district);
                    blockFilterSpinner(District.get(position).getDistictCode());
                    prefManager.setDistrictCode(District.get(position).getDistictCode());

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_block.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    all_block.setChecked(true);
                    prefManager.setBlockName("All");
                    loadOfflineVillgeListDBValues();
                } else {
                    all_block.setChecked(false);
                    pref_Block = Block.get(position).getBlockName();
                    prefManager.setBlockName(pref_Block);
//                    prefManager.setBlockCode(Block.get(position).getBlockCode());
                    prefManager.setKeySpinnerSelectedBlockcode(Block.get(position).getBlockCode());
                    villageFilterSpinner(Block.get(position).getBlockCode());

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sp_village.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    all_village.setChecked(true);
                    prefManager.setVillageListPvName("All");
                } else {
                    all_village.setChecked(false);
                    pref_Village = Village.get(position).getVillageListPvName();
                    prefManager.setVillageListPvName(pref_Village);
//                    prefManager.setKeySpinnerSelectedPvcode(Village.get(position).getVillageListPvCode());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_scheme.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    all_scheme.setChecked(true);
                    prefManager.setSchemeName("All");
                } else {
                    all_scheme.setChecked(false);
                    pref_Scheme = Scheme.get(position).getSchemeName();
                    prefManager.setSchemeName(pref_Scheme);
//                    prefManager.setKeySpinnerSelectedSchemeSeqId(Scheme.get(position).getSchemeID());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        sp_financialYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    pref_finYear = FinYearList.get(position).getFinancialYear();
                    prefManager.setFinancialyearName(pref_finYear);
                    loadOfflineSchemeListDBValues(pref_finYear);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (prefManager.getLevels().equalsIgnoreCase("B")) {
            villageFilterSpinner(prefManager.getBlockCode());
        }
    }

    public void checkdata_offline() { // In order to check worklist available for processing inspection or action based on login

        Cursor worklist = getRawEvents("SELECT * FROM " + DBHelper.WORK_LIST_OPTIONAL, null);
        if (worklist.getCount() > 0) {
            loadOfflineDBValues();
        } else {
            Utils.showAlert(this, "Please download the data first,then come here for further process");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                }
            }, SPLASH_TIME_OUT);
        }
    }

    public void villageFilterSpinner(String filterVillage) {
        String villageSql = "select b.dcode as dcode,b.bcode as bcode,b.pvcode as pvcode,b.pvname as pvname from (select pvcode,bcode,dcode from " + DBHelper.WORK_LIST_OPTIONAL + " where dcode = " + prefManager.getDistrictCode() + " and bcode ='" + filterVillage + "' group by pvcode)a left outer join (select * from " + DBHelper.VILLAGE_TABLE_NAME + " where dcode = " + prefManager.getDistrictCode() + " and bcode ='" + filterVillage + "')b on a.pvcode = b.pvcode and a.bcode = b.bcode order by pvname";
        Log.d("villageSql", "" + villageSql);
        Cursor VillageList = getRawEvents(villageSql, null);
        Village.clear();
        BlockListValue villageListValue = new BlockListValue();
        villageListValue.setVillageListPvName("Select Village");
        Village.add(villageListValue);
        if (VillageList.getCount() > 0) {
            if (VillageList.moveToFirst()) {
                do {
                    BlockListValue villageList = new BlockListValue();
                    String districtCode = VillageList.getString(VillageList.getColumnIndexOrThrow(AppConstant.DISTRICT_CODE));
                    String blockCode = VillageList.getString(VillageList.getColumnIndexOrThrow(AppConstant.BLOCK_CODE));
                    String pvCode = VillageList.getString(VillageList.getColumnIndexOrThrow(AppConstant.PV_CODE));
                    String pvname = VillageList.getString(VillageList.getColumnIndexOrThrow(AppConstant.PV_NAME));

                    villageList.setVillageListDistrictCode(districtCode);
                    villageList.setVillageListBlockCode(blockCode);
                    villageList.setVillageListPvCode(pvCode);
                    villageList.setVillageListPvName(pvname);

                    Village.add(villageList);
                    Log.d("spinnersize", "" + Village.size());
                } while (VillageList.moveToNext());
            }
        }
        sp_village.setAdapter(new CommonAdapter(this, Village, "VillageList"));
    }

    public void blockFilterSpinner(String filterBlock) {

        String blocksql = "select a.bcode as bcode,b.dcode as dcode,b.bname as bname from (select  bcode,dcode from " + DBHelper.WORK_LIST_OPTIONAL + " where dcode = " + filterBlock + "  group by bcode)a left join (select * from " + DBHelper.BLOCK_TABLE_NAME + " where dcode = " + filterBlock + ")b on a.bcode = b.bcode and a.dcode = b.dcode order by bname";
        Log.d("blocksql", blocksql);
        Cursor BlockList = getRawEvents(blocksql, null);
        Block.clear();
        BlockListValue blockListValue = new BlockListValue();
        blockListValue.setBlockName("Select Block");
        Block.add(blockListValue);
        if (BlockList.getCount() > 0) {
            if (BlockList.moveToFirst()) {
                do {
                    BlockListValue blockList = new BlockListValue();
                    String districtCode = BlockList.getString(BlockList.getColumnIndexOrThrow(AppConstant.DISTRICT_CODE));
                    String blockCode = BlockList.getString(BlockList.getColumnIndexOrThrow(AppConstant.BLOCK_CODE));
                    String blockName = BlockList.getString(BlockList.getColumnIndexOrThrow(AppConstant.BLOCK_NAME));
                    blockList.setDistictCode(districtCode);
                    blockList.setBlockCode(blockCode);
                    blockList.setBlockName(blockName);
                    Block.add(blockList);
                } while (BlockList.moveToNext());
            }
        }
        sp_block.setAdapter(new CommonAdapter(this, Block, "BlockList"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.homeimg:
                dashboard();
                break;
            case R.id.btn_save:
                if (prefManager.getLevels().equalsIgnoreCase("S")) {
                    projectListScreenStateUser();
                } else if (prefManager.getLevels().equalsIgnoreCase("SD")) {
                    // projectListScreenSubDivisionUser();
                } else if (prefManager.getLevels().equalsIgnoreCase("D")) {
                    projectListScreenDistrictUser();
                } else if (prefManager.getLevels().equalsIgnoreCase("B")) {
                    projectListScreenBlockUser();
                }
                break;
            case R.id.backimg:
                onBackPress();
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

    public void projectListScreenStateUser() {
        if (!"Select Financial year".equalsIgnoreCase(FinYearList.get(sp_financialYear.getSelectedItemPosition()).getFinancialYear())) {
            if (!"Select District".equalsIgnoreCase(District.get(sp_district.getSelectedItemPosition()).getDistrictName())) {
                if (!"Select Block".equalsIgnoreCase(Block.get(sp_block.getSelectedItemPosition()).getBlockName())) {
                    if (!"Select Village".equalsIgnoreCase(Village.get(sp_village.getSelectedItemPosition()).getVillageListPvName())) {
                        if (!"Select Scheme".equalsIgnoreCase(Scheme.get(sp_scheme.getSelectedItemPosition()).getSchemeName())) {
                            goto_next();
                        } else {
                            Utils.showAlert(this, "Select Scheme");
                        }
                    } else {
                        Utils.showAlert(this, "Select Village");
                    }
                } else {
                    Utils.showAlert(this, "Select Block");
                }
            } else {
                Utils.showAlert(this, "Select District");
            }
        } else {
            Utils.showAlert(this, "Select Financial year");
        }
    }

    public void projectListScreenDistrictUser() {

        if (!"Select Financial year".equalsIgnoreCase(FinYearList.get(sp_financialYear.getSelectedItemPosition()).getFinancialYear())) {
            if (!"Select Block".equalsIgnoreCase(Block.get(sp_block.getSelectedItemPosition()).getBlockName())) {
                if (!"Select Village".equalsIgnoreCase(Village.get(sp_village.getSelectedItemPosition()).getVillageListPvName())) {
                    if (!"Select Scheme".equalsIgnoreCase(Scheme.get(sp_scheme.getSelectedItemPosition()).getSchemeName())) {
                        goto_next();
                    } else {
                        Utils.showAlert(this, "Select Scheme");
                    }
                } else {
                    Utils.showAlert(this, "Select Village");
                }
            } else {
                Utils.showAlert(this, "Select Block");
            }
        } else {
            Utils.showAlert(this, "Select Financial year");
        }
    }

    public void projectListScreenBlockUser() {

        if (!"Select Financial year".equalsIgnoreCase(FinYearList.get(sp_financialYear.getSelectedItemPosition()).getFinancialYear())) {
            if (!"Select Village".equalsIgnoreCase(Village.get(sp_village.getSelectedItemPosition()).getVillageListPvName())) {
                if (!"Select Scheme".equalsIgnoreCase(Scheme.get(sp_scheme.getSelectedItemPosition()).getSchemeName())) {
                    goto_next();

                } else {
                    Utils.showAlert(this, "Select Scheme");
                }
            } else {
                Utils.showAlert(this, "Select Village");
            }
        } else {
            Utils.showAlert(this, "Select Financial year");
        }
    }


    public void goto_next() {
        String blockCode;
        String districtCode = null;
        if (prefManager.getLevels().equalsIgnoreCase("S")) {
            districtCode = District.get(sp_district.getSelectedItemPosition()).getDistictCode();
            blockCode = Block.get(sp_block.getSelectedItemPosition()).getBlockCode();
        } else if (prefManager.getLevels().equalsIgnoreCase("D")) {
            districtCode = prefManager.getDistrictCode();
            blockCode = Block.get(sp_block.getSelectedItemPosition()).getBlockCode();
        } else {
            districtCode = prefManager.getDistrictCode();
            blockCode = prefManager.getBlockCode();
        }
        String pvCode = Village.get(sp_village.getSelectedItemPosition()).getVillageListPvCode();
        String sequentialID = Scheme.get(sp_scheme.getSelectedItemPosition()).getSchemeSequentialID();

        String highValueProject = null;


        if (high_value_projects.isChecked()) {
            highValueProject = "Y";
        }

        Intent intent = new Intent(this, ProjectListScreen.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(AppConstant.BLOCK_CODE, blockCode);
        intent.putExtra(AppConstant.DISTRICT_CODE, districtCode);
        intent.putExtra(AppConstant.PV_CODE, pvCode);
        intent.putExtra(AppConstant.SCHEME_SEQUENTIAL_ID, sequentialID);
        intent.putExtra(AppConstant.IS_HIGH_VALUE_PROJECT, highValueProject);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

    }

    public void loadOfflineDBValues() {
        if (prefManager.getLevels().equalsIgnoreCase("D")) {
            loadOfflineBlockListDBValues();
        }
        if (!prefManager.getLevels().equalsIgnoreCase("B")) {
            loadOfflineVillgeListDBValues();
        }
        if (prefManager.getLevels().equalsIgnoreCase("S")) {
            loadOfflineDistrictListDBValues();
        }
        loadOfflineFinYearListDBValues();
    }

    public void loadOfflineDistrictListDBValues() {

        Cursor DistrictList = getRawEvents("select a.dcode as dcode,b.dname as dname from (select dcode from " + DBHelper.WORK_LIST_OPTIONAL + "  group by dcode)a \n" +
                "left join (select * from " + DBHelper.DISTRICT_TABLE_NAME + ")b \n" +
                "on a.dcode = b.dcode order by dname", null);
        // Cursor BlockList = getRawEvents("SELECT * FROM " + BLOCK_TABLE_NAME, null);
        District.clear();
        BlockListValue districtListValue = new BlockListValue();
        districtListValue.setDistrictName("Select District");
        District.add(districtListValue);
        if (DistrictList.getCount() > 0) {
            if (DistrictList.moveToFirst()) {
                do {
                    BlockListValue districtList = new BlockListValue();
                    String districtCode = DistrictList.getString(DistrictList.getColumnIndexOrThrow(AppConstant.DISTRICT_CODE));
                    String districtName = DistrictList.getString(DistrictList.getColumnIndexOrThrow(AppConstant.DISTRICT_NAME));
                    districtList.setDistictCode(districtCode);
                    districtList.setDistrictName(districtName);
                    District.add(districtList);
                } while (DistrictList.moveToNext());
            }
        }
        sp_district.setAdapter(new CommonAdapter(this, District, "DistrictList"));
    }

    public void loadOfflineBlockListDBValues() {
        String blocksql = "select a.bcode as bcode,b.dcode as dcode,b.bname as bname from (select  bcode,dcode from " + DBHelper.WORK_LIST_OPTIONAL + " where dcode = " + prefManager.getDistrictCode() + "  group by bcode)a left join (select * from " + DBHelper.BLOCK_TABLE_NAME + " where dcode = " + prefManager.getDistrictCode() + ")b on a.bcode = b.bcode and a.dcode = b.dcode order by bname";
        Cursor BlockList = getRawEvents(blocksql, null);
        // Cursor BlockList = getRawEvents("SELECT * FROM " + BLOCK_TABLE_NAME, null);
        Block.clear();
        BlockListValue blockListValue = new BlockListValue();
        blockListValue.setBlockName("Select Block");
        Block.add(blockListValue);
        if (BlockList.getCount() > 0) {
            if (BlockList.moveToFirst()) {
                do {
                    BlockListValue blockList = new BlockListValue();
                    String districtCode = BlockList.getString(BlockList.getColumnIndexOrThrow(AppConstant.DISTRICT_CODE));
                    String blockCode = BlockList.getString(BlockList.getColumnIndexOrThrow(AppConstant.BLOCK_CODE));
                    String blockName = BlockList.getString(BlockList.getColumnIndexOrThrow(AppConstant.BLOCK_NAME));
                    blockList.setDistictCode(districtCode);
                    blockList.setBlockCode(blockCode);
                    blockList.setBlockName(blockName);
                    Block.add(blockList);
                } while (BlockList.moveToNext());
            }
        }
        sp_block.setAdapter(new CommonAdapter(this, Block, "BlockList"));
    }

    public void loadOfflineVillgeListDBValues() {

        Cursor VillageList = getRawEvents("SELECT * FROM " + VILLAGE_TABLE_NAME + " order by pvname asc", null);
        Village.clear();
        BlockListValue villageListValue = new BlockListValue();
        villageListValue.setVillageListPvName("Select Village");
        Village.add(villageListValue);
        if (VillageList.getCount() > 0) {
            if (VillageList.moveToFirst()) {
                do {
                    BlockListValue villageList = new BlockListValue();
                    String districtCode = VillageList.getString(VillageList.getColumnIndexOrThrow(AppConstant.DISTRICT_CODE));
                    String blockCode = VillageList.getString(VillageList.getColumnIndexOrThrow(AppConstant.BLOCK_CODE));
                    String pvCode = VillageList.getString(VillageList.getColumnIndexOrThrow(AppConstant.PV_CODE));
                    String pvname = VillageList.getString(VillageList.getColumnIndexOrThrow(AppConstant.PV_NAME));

                    villageList.setVillageListDistrictCode(districtCode);
                    villageList.setVillageListBlockCode(blockCode);
                    villageList.setVillageListPvCode(pvCode);
                    villageList.setVillageListPvName(pvname);

                    Village.add(villageList);
                } while (VillageList.moveToNext());
            }
        }
        sp_village.setAdapter(new CommonAdapter(this, Village, "VillageList"));
    }

    public void loadOfflineSchemeListDBValues(String fin_Year) {
        //  String query = "SELECT * FROM " + DBHelper.SCHEME_TABLE_NAME + " Where fin_year = '" + fin_Year + "'";
        String query = "select b.scheme_seq_id as scheme_seq_id,b.scheme_name as scheme_name,b.fin_year as fin_year from (SELECT * FROM " + DBHelper.WORK_LIST_OPTIONAL + " where fin_year = '" + fin_Year + "' group by scheme_id)a left join (select * from " + DBHelper.SCHEME_TABLE_NAME + " where fin_year = '" + fin_Year + "')b on a.scheme_id = b.scheme_seq_id and a.fin_year = b.fin_year order by scheme_name";
        Cursor SchemeList = getRawEvents(query, null);
        Log.d("SchemeQuery", "" + query);

        Scheme.clear();
        BlockListValue schemeListValue = new BlockListValue();
        schemeListValue.setSchemeName("Select Scheme");
        Scheme.add(schemeListValue);
        if (SchemeList.getCount() > 0) {
            if (SchemeList.moveToFirst()) {
                do {
                    BlockListValue schemeList = new BlockListValue();
                    String schemeSequentialID = SchemeList.getString(SchemeList.getColumnIndexOrThrow(AppConstant.SCHEME_SEQUENTIAL_ID));
                    String schemeName = SchemeList.getString(SchemeList.getColumnIndexOrThrow(AppConstant.SCHEME_NAME));
                    String fin_year = SchemeList.getString(SchemeList.getColumnIndexOrThrow(AppConstant.FINANCIAL_YEAR));
                    schemeList.setSchemeSequentialID(schemeSequentialID);
                    schemeList.setSchemeName(schemeName);
                    schemeList.setFinancialYear(fin_year);
                    Scheme.add(schemeList);

                } while (SchemeList.moveToNext());
            }
        }
        sp_scheme.setAdapter(new CommonAdapter(this, Scheme, "SchemeList"));
    }

    public void loadOfflineFinYearListDBValues() {
        FinYearList.clear();
        //Cursor FinYear = getRawEvents("SELECT fin_year FROM " + DBHelper.FINANCIAL_YEAR_TABLE_NAME, null);
        Cursor FinYear = getRawEvents("select  fin_year from " + DBHelper.WORK_LIST_OPTIONAL + " group by fin_year", null);

        BlockListValue finYearListValue = new BlockListValue();
        finYearListValue.setFinancialYear("Select Financial year");
        FinYearList.add(finYearListValue);
        if (FinYear.getCount() > 0) {
            if (FinYear.moveToFirst()) {
                do {
                    BlockListValue finyearList = new BlockListValue();
                    String financialYear = FinYear.getString(FinYear.getColumnIndexOrThrow(AppConstant.FINANCIAL_YEAR));
                    finyearList.setFinancialYear(financialYear);
                    FinYearList.add(finyearList);
                    //   Log.d("finyeardb", "" + finyearList);
                } while (FinYear.moveToNext());
            }
        }

        sp_financialYear.setAdapter(new CommonAdapter(this, FinYearList, "FinYearList"));
    }

    public void onBackPress() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }

    @Override
    public void OnMyResponse(ServerResponse serverResponse) {

    }


    @Override
    public void OnError(VolleyError volleyError) {

    }

    public Cursor getRawEvents(String sql, String string) {
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //   checkdata_offline();
    }
}
