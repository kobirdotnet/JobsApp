package com.app.jobs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.util.Constant;
import com.example.util.JsonUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;

public class ProfileActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView txtName, txtEmail, txtPhone, txtCity, txtAddress;
    ScrollView mScrollView;
    ProgressBar mProgressBar;
    MyApplication MyApp;
    ShapedImageView imageUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.menu_profile));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        MyApp = MyApplication.getInstance();
        imageUser = (ShapedImageView) findViewById(R.id.image_profile);
        txtName = (TextView) findViewById(R.id.text_name);
        txtEmail = (TextView) findViewById(R.id.text_email);
        txtPhone = (TextView) findViewById(R.id.text_telephone);
        txtCity = (TextView) findViewById(R.id.text_city);
        txtAddress = (TextView) findViewById(R.id.text_address);

        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);

        if (JsonUtils.isNetworkAvailable(ProfileActivity.this)) {
            new MyTaskProfile().execute(Constant.USER_PROFILE_URL + MyApp.getUserId());
        } else {
            showToast(getString(R.string.conne_msg1));
        }
    }

    private class MyTaskProfile extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            mScrollView.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mProgressBar.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
            if (null == result || result.length() == 0) {
                showToast(getString(R.string.nodata));
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        txtName.setText(objJson.getString(Constant.USER_NAME));
                        txtEmail.setText(objJson.getString(Constant.USER_EMAIL));
                        txtPhone.setText(objJson.getString(Constant.USER_PHONE));
                        if (objJson.getString(Constant.USER_CITY).isEmpty()) {
                            txtCity.setText(getString(R.string.please_update_city));
                        } else {
                            txtCity.setText(objJson.getString(Constant.USER_CITY));
                        }

                        if (objJson.getString(Constant.USER_ADDRESS).isEmpty()) {
                            txtAddress.setText(getString(R.string.please_update_address));
                        } else {
                            txtAddress.setText(objJson.getString(Constant.USER_ADDRESS));
                        }

                        if (!objJson.getString(Constant.USER_IMAGE).isEmpty()) {
                            Picasso.with(ProfileActivity.this).load(objJson.getString(Constant.USER_IMAGE)).into(imageUser);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showToast(String msg) {
        Toast.makeText(ProfileActivity.this, msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_edit:
                Intent profile = new Intent(ProfileActivity.this, EditProfileActivity.class);
                profile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(profile);
                break;
            case R.id.menu_logout:
                Logout();
                break;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void Logout() {
        new AlertDialog.Builder(ProfileActivity.this)
                .setTitle(getString(R.string.menu_logout))
                .setMessage(getString(R.string.logout_msg))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MyApp.saveIsLogin(false);
                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                //  .setIcon(R.drawable.ic_logout)
                .show();
    }
}
