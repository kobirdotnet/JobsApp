package com.app.jobs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.util.Constant;
import com.example.util.JsonUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.mobsandgeeks.saripaar.annotation.TextRule;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.nguyenhoanglam.imagepicker.activity.ImagePicker;
import com.nguyenhoanglam.imagepicker.activity.ImagePickerActivity;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;
import cz.msebera.android.httpclient.Header;


public class EditProfileActivity extends AppCompatActivity implements Validator.ValidationListener {

    @Required(order = 1)
    @TextRule(order = 2, minLength = 3, maxLength = 35, trim = true, message = "Enter Valid Full Name")
    EditText edtFullName;

    @Required(order = 3)
    @Email(order = 4, message = "Please Check and Enter a valid Email Address")
    EditText edtEmail;

    @Required(order = 5)
    @Password(order = 6, message = "Enter a Valid Password")
    @TextRule(order = 7, minLength = 6, message = "Enter a Password Correctly")
    EditText edtPassword;

    @TextRule(order = 8, message = "Enter valid Phone Number", minLength = 0, maxLength = 14)
    EditText edtMobile;

    @TextRule(order = 9, message = "Enter City", minLength = 0, maxLength = 35)
    EditText edtCity;

    @TextRule(order = 10, message = "Enter Address", minLength = 0, maxLength = 100)
    EditText edtAddress;

    private Validator validator;

    MyApplication MyApp;

    String strFullname, strEmail, strPassword, strMobi, strCity, strAddress;

    Toolbar toolbar;
    ScrollView mScrollView;
    ProgressBar mProgressBar;

    TextView txtChooseImage, txtChooseResume, txtResumeName;
    ImageView imgChoose;

    private int REQUEST_FEATURED_PICKER = 2000;
    public int FILE_PICKER_REQUEST_CODE = 3000;

    private ArrayList<Image> featuredImages = new ArrayList<>();
    String docPath;
    boolean isFeatured = false;
    boolean isResume = false;
    ProgressDialog pDialog;
    LinearLayout lytResume;
    ShapedImageView imageUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.menu_edit_profile));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        MyApp = MyApplication.getInstance();
        lytResume = (LinearLayout) findViewById(R.id.lytResume);
        edtFullName = (EditText) findViewById(R.id.edt_name);
        edtEmail = (EditText) findViewById(R.id.edt_email);
        edtPassword = (EditText) findViewById(R.id.edt_password);
        edtMobile = (EditText) findViewById(R.id.edt_phone);
        edtCity = (EditText) findViewById(R.id.edt_city);
        edtAddress = (EditText) findViewById(R.id.edt_address);
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
        pDialog = new ProgressDialog(this);
        txtChooseImage = (TextView) findViewById(R.id.btnChooseFeatured);
        txtChooseResume = (TextView) findViewById(R.id.btnChooseResume);
        txtResumeName = (TextView) findViewById(R.id.textResume);
        imgChoose = (ImageView) findViewById(R.id.imageFeatured);
        imageUser = (ShapedImageView) findViewById(R.id.image_profile);

        if (MyApp.getIsJobProvider()) {
            lytResume.setVisibility(View.GONE);
        }

        if (JsonUtils.isNetworkAvailable(EditProfileActivity.this)) {
            new MyTaskProfile().execute(Constant.USER_PROFILE_URL + MyApp.getUserId());
        } else {
            showToast(getString(R.string.conne_msg1));
        }

        txtChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseFeaturedImage();
            }
        });

        txtChooseResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFilePicker();
            }
        });

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Override
    public void onValidationSucceeded() {
        if (JsonUtils.isNetworkAvailable(EditProfileActivity.this)) {
            uploadData();
        } else {
            showToast(getString(R.string.conne_msg1));
        }
    }

    @Override
    public void onValidationFailed(View failedView, Rule<?> failedRule) {
        String message = failedRule.getFailureMessage();
        if (failedView instanceof EditText) {
            failedView.requestFocus();
            ((EditText) failedView).setError(message);
        } else {
            Toast.makeText(this, "Record Not Saved", Toast.LENGTH_SHORT).show();
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
                        edtFullName.setText(objJson.getString(Constant.USER_NAME));
                        edtEmail.setText(objJson.getString(Constant.USER_EMAIL));
                        edtMobile.setText(objJson.getString(Constant.USER_PHONE));
                        if (!objJson.getString(Constant.USER_CITY).isEmpty()) {
                            edtCity.setText(objJson.getString(Constant.USER_CITY));
                        }
                        if (!objJson.getString(Constant.USER_ADDRESS).isEmpty()) {
                            edtAddress.setText(objJson.getString(Constant.USER_ADDRESS));
                        }
                        if (!objJson.getString(Constant.USER_IMAGE).isEmpty()) {
                            Picasso.with(EditProfileActivity.this).load(objJson.getString(Constant.USER_IMAGE)).into(imageUser);
                            Picasso.with(EditProfileActivity.this).load(objJson.getString(Constant.USER_IMAGE)).into(imgChoose);
                        }
                        if (!objJson.getString(Constant.USER_RESUME).isEmpty()) {
                            txtResumeName.setText(objJson.getString(Constant.USER_RESUME));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showToast(String msg) {
        Toast.makeText(EditProfileActivity.this, msg, Toast.LENGTH_LONG).show();
    }


    public void setResult() {

        if (Constant.GET_SUCCESS_MSG == 0) {
            showToast("Failed");
        } else {
            showToast("Your Profile Updated");
            ActivityCompat.finishAffinity(EditProfileActivity.this);
            Intent intent = new Intent(getApplicationContext(), MyApp.getIsJobProvider() ? JobProviderMainActivity.class : MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        menu.getItem(0).setIcon(R.drawable.ic_save);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_edit:
                validator.validateAsync();
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
        new AlertDialog.Builder(EditProfileActivity.this)
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

    public void chooseFeaturedImage() {
        ImagePicker.create(this)
                .folderMode(true)
                .folderTitle("Folder")
                .imageTitle("Tap to select")
                .single()
                .limit(1)
                .showCamera(false)
                .imageDirectory("Camera")
                .start(REQUEST_FEATURED_PICKER);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FEATURED_PICKER) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                featuredImages = data.getParcelableArrayListExtra(ImagePickerActivity.INTENT_EXTRA_SELECTED_IMAGES);
                Uri uri = Uri.fromFile(new File(featuredImages.get(0).getPath()));
                Picasso.with(EditProfileActivity.this).load(uri).into(imgChoose);
                isFeatured = true;
            }
        } else if (requestCode == FILE_PICKER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                docPath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                txtResumeName.setText(docPath.substring(docPath.lastIndexOf("/") + 1));
                isResume = true;
            }
        }
    }

    private void openFilePicker() {
        new MaterialFilePicker()
                .withActivity(this)
                .withRequestCode(FILE_PICKER_REQUEST_CODE)
                .withHiddenFiles(false)
                .withFilter(Pattern.compile(".*\\.pdf$"))
                .withTitle("Tap to Open")
                .start();
    }

    public void showProgressDialog() {
        pDialog.setMessage(getString(R.string.loading));
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(true);
        pDialog.show();
    }

    public void dismissProgressDialog() {
        pDialog.dismiss();
    }

    public void uploadData() {
        strFullname = edtFullName.getText().toString();
        strEmail = edtEmail.getText().toString();
        strPassword = edtPassword.getText().toString();
        strMobi = edtMobile.getText().toString();
        strCity = edtCity.getText().toString();
        strAddress = edtAddress.getText().toString();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("name", strFullname);
        params.put("email", strEmail);
        params.put("password", strPassword);
        params.put("phone", strMobi);
        params.put("user_id", MyApp.getUserId());
        params.put("city", strCity);
        params.put("address", strAddress);
        if (isFeatured) {
            try {
                params.put("user_image", new File(featuredImages.get(0).getPath()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (isResume) {
            try {
                params.put("user_resume", new File(docPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        client.post(Constant.USER_PROFILE_UPDATE_URL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showProgressDialog();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Log.e("Response", new String(responseBody));
                dismissProgressDialog();
                String result = new String(responseBody);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray jsonArray = jsonObject.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson = jsonArray.getJSONObject(0);
                    Constant.GET_SUCCESS_MSG = objJson.getInt(Constant.SUCCESS);
                    setResult();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dismissProgressDialog();
            }

        });
    }
}
