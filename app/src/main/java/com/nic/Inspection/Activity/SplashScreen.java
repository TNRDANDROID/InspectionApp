package com.nic.Inspection.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.nic.Inspection.Helper.AppVersionHelper;
import com.nic.Inspection.R;
import com.nic.Inspection.Utils.Utils;
import com.nic.Inspection.api.Api;
import com.nic.Inspection.api.ServerResponse;
import com.nic.Inspection.session.PrefManager;

import org.json.JSONObject;

;

/**
 * Created by AchanthiSundar on 28-12-2018.
 */

public class SplashScreen extends AppCompatActivity implements View.OnClickListener, Api.ServerResponseListener,AppVersionHelper.myAppVersionInterface {
    private TextView textView;
    private Button button;
    private static int SPLASH_TIME_OUT = 2000;
    private PrefManager prefManager;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        prefManager = new PrefManager(this);
        if(Utils.isOnline()){
            checkAppVersion();
        }else{
            showSignInScreen();

        }
    }


    @Override
    public void onClick(View v) {
//        if (v.equals(button)) {
//            try {
//                callSampleApi();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }

    /* public class SendPostRequest extends AsyncTask<String, Void, String> {

         protected void onPreExecute(){}

         protected String doInBackground(String... arg0) {

             try {

                 URL url = new URL("https://www.tnrd.gov.in/project/webservices_forms/inspection/login_services.php"); // here is your URL path

                 JSONObject postDataParams = new JSONObject();
                 postDataParams.put("name", "abc");
                 postDataParams.put("email", "abc@gmail.com");
                 Log.e("params",postDataParams.toString());

                 HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                 conn.setReadTimeout(15000 *//* milliseconds *//*);
                conn.setConnectTimeout(15000 *//* milliseconds *//*);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(getApplicationContext(), result,
                    Toast.LENGTH_LONG).show();
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }*/

    private void showSignInScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent i = new Intent(SplashScreen.this, LoginScreen.class);

                startActivity(i);
                finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        }, SPLASH_TIME_OUT);
    }

    private void checkAppVersion() {
        new AppVersionHelper(this, SplashScreen.this).callAppVersionCheckApi();
    }

    @Override
    public void onAppVersionCallback(String value) {
        if (value.length() > 0 && "Update".equalsIgnoreCase(value)) {
            startActivity(new Intent(this, AppVersionActivity.class));
            finish();
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        } else {
                showSignInScreen();
            }

    }


    @Override
    public void OnMyResponse(ServerResponse serverResponse) {
        JSONObject responseInnerJSONObj = null;
        String loginResponse = null;
        loginResponse = serverResponse.getResponse();
        textView.setText(loginResponse);
//        Toast.makeText(this,loginResponse,Toast.LENGTH_LONG).show();
//            int status = responseInnerJSONObj.getInt("Status");
//            String message = responseInnerJSONObj.getString(AppConstant.KEY_MESSAGE);
//
//            if (status == 1) {
//                if ("SignIn".equalsIgnoreCase(apiType)) {
//                    JSONObject userJSONObject = responseInnerJSONObj.getJSONObject(AppConstant.KEY_USER);
//                    String id = userJSONObject.getString(AppConstant.KEY_USER_ID);
//                    String username = userJSONObject.getString(AppConstant.KEY_USER_NAME);
//                }
//            }

    }

    @Override
    public void OnError(VolleyError volleyError) {

    }
}
