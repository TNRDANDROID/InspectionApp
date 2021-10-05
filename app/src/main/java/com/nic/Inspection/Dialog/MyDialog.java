package com.nic.Inspection.Dialog;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.nic.Inspection.DataBase.DBHelper;
import com.nic.Inspection.R;
import com.nic.Inspection.Support.MyCustomTextView;

import static com.nic.Inspection.Activity.LoginScreen.db;


/**
 * Created by user on 04-06-2016.
 */
public class MyDialog {
    public myOnClickListener myListener;
//    private PrefManager prefManager;

    public MyDialog(Activity context) {
//        prefManager         = new PrefManager(context);
        this.myListener = (myOnClickListener) context;
    }

    // This is my interface //
    public interface myOnClickListener {
        void onButtonClick(AlertDialog alertDialog, String type);

    }

    public void exitDialog(final Activity activity, String message, final String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.alert_dialog, null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setView(dialogView, 0, 0, 0, 0);
        alertDialog.setCancelable(false);
        alertDialog.show();

        MyCustomTextView tv_message = (MyCustomTextView) dialogView.findViewById(R.id.tv_message);
        tv_message.setText(message);

        Button btnOk = (Button) dialogView.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myListener.onButtonClick(alertDialog, type);
//                if(!type.equalsIgnoreCase("Exit")) {
//                    prefManager.clearSession();
//                    Crashlytics.setUserEmail(null);
//                    Crashlytics.setUserName(null);
//                    Crashlytics.setUserIdentifier(null);
//                    DatabaseHandler db = new DatabaseHandler(activity);
//                    db.deleteAll();
//                    ImageLoader imgLoader = new ImageLoader(activity);
//                    imgLoader.clearCache();
//                }
                if(type.equalsIgnoreCase("Logout")) {
                    try {
                        db.delete(DBHelper.DISTRICT_TABLE_NAME, null, null);
                        db.delete(DBHelper.BLOCK_TABLE_NAME, null, null);
                        db.delete(DBHelper.VILLAGE_TABLE_NAME, null, null);
                        db.delete(DBHelper.SCHEME_TABLE_NAME, null, null);
                        db.delete(DBHelper.FINANCIAL_YEAR_TABLE_NAME, null, null);
                        db.delete(DBHelper.WORK_STAGE_TABLE, null, null);
                        db.delete(DBHelper.WORK_LIST_OPTIONAL, null, null);
                        db.delete(DBHelper.OBSERVATION_TABLE,   null, null);
                        db.execSQL(String.format("DELETE FROM " + DBHelper.INSPECTION_ACTION + " WHERE delete_flag=1;", null));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Button btnCancel = (Button) dialogView.findViewById(R.id.btn_cancel);
        btnCancel.setVisibility(View.VISIBLE);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
    }
}
