package com.app.jobs;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.item.ItemCategory;
import com.example.util.Constant;
import com.example.util.JsonUtils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mobsandgeeks.saripaar.Rule;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.Required;
import com.mobsandgeeks.saripaar.annotation.TextRule;
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

import cz.msebera.android.httpclient.Header;
import io.blackbox_vision.datetimepickeredittext.view.DatePickerEditText;

public class AddJobActivity extends AppCompatActivity implements Validator.ValidationListener {

    @Required(order = 1)
    EditText edtTitle;
    @Required(order = 2)
    EditText edtDesignation;
    @Required(order = 3)
    EditText edtDescription;
    @Required(order = 4)
    EditText edtSalary;
    @Required(order = 5)
    EditText edtCompanyName;
    @Required(order = 6)
    EditText edtWebsite;
    @Required(order = 7)
    @TextRule(order = 8, message = "Enter valid Phone Number", minLength = 0, maxLength = 14)
    EditText edtPhone;
    @Required(order = 9)
    @Email(order = 10, message = "Please Check and Enter a valid Email Address")
    EditText edtEmail;
    @Required(order = 11)
    EditText edtVacancy;
    @Required(order = 12)
    EditText edtAddress;
    @Required(order = 13)
    EditText edtQualification;
    @Required(order = 14)
    EditText edtSkills;
    @Required(order = 15)
    DatePickerEditText edtDate;

    private int REQUEST_GALLERY_PICKER = 2001;
    private Validator validator;
    private ArrayList<Image> galleryImages = new ArrayList<>();
    boolean isFeatured = false;
    ProgressDialog pDialog;
    Button btnSave;
    ImageView imgChoose;
    TextView textChoose;
    MyApplication MyApp;
    Toolbar toolbar;
    ScrollView mScrollView;
    ProgressBar mProgressBar;
    Spinner spCategory;
    ArrayList<ItemCategory> mCategoryList;
    ArrayList<String> mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_job);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.add_job));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mScrollView = (ScrollView) findViewById(R.id.scrollView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);

        pDialog = new ProgressDialog(this);
        edtDate = (DatePickerEditText) findViewById(R.id.edt_date);
        edtDate.setManager(getSupportFragmentManager());
        MyApp = MyApplication.getInstance();
        edtTitle = (EditText) findViewById(R.id.edt_name);
        edtDesignation = (EditText) findViewById(R.id.edt_designation);
        edtDescription = (EditText) findViewById(R.id.edt_description);
        edtSalary = (EditText) findViewById(R.id.edt_salary);
        edtCompanyName = (EditText) findViewById(R.id.edt_company_name);
        edtWebsite = (EditText) findViewById(R.id.edt_website);
        edtPhone = (EditText) findViewById(R.id.edt_phone);
        edtEmail = (EditText) findViewById(R.id.edt_email);
        edtVacancy = (EditText) findViewById(R.id.edt_vacancy);
        edtAddress = (EditText) findViewById(R.id.edt_address);
        edtQualification = (EditText) findViewById(R.id.edt_qualification);
        edtSkills = (EditText) findViewById(R.id.edt_skill);
        btnSave = (Button) findViewById(R.id.button_save);
        imgChoose = (ImageView) findViewById(R.id.imageFeatured);
        textChoose = (TextView) findViewById(R.id.btnChooseFeatured);
        spCategory = (Spinner) findViewById(R.id.spCategory);
        mCategoryList = new ArrayList<>();
        mName = new ArrayList<>();
        textChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseGalleryImage();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validator.validateAsync();
            }
        });

        if (JsonUtils.isNetworkAvailable(AddJobActivity.this)) {
            getCategory();
        } else {
            Toast.makeText(AddJobActivity.this, getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
        }

        validator = new Validator(this);
        validator.setValidationListener(this);
    }

    @Override
    public void onValidationSucceeded() {
        if (JsonUtils.isNetworkAvailable(AddJobActivity.this)) {
            if (isFeatured) {
                addJob();
            } else {
                Toast.makeText(AddJobActivity.this, getString(R.string.required), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(AddJobActivity.this, getString(R.string.conne_msg1), Toast.LENGTH_SHORT).show();
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

    public void chooseGalleryImage() {
        ImagePicker.create(this)
                .folderMode(true)
                .folderTitle("Folder")
                .imageTitle("Tap to select")
                .multi()
                .limit(1)
                .showCamera(false)
                .imageDirectory("Camera")
                .start(REQUEST_GALLERY_PICKER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GALLERY_PICKER) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                galleryImages = data.getParcelableArrayListExtra(ImagePickerActivity.INTENT_EXTRA_SELECTED_IMAGES);
                Uri uri = Uri.fromFile(new File(galleryImages.get(0).getPath()));
                Picasso.with(AddJobActivity.this).load(uri).into(imgChoose);
                isFeatured = true;
            }
        }
    }

    private void getCategory() {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constant.CATEGORY_URL, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                mProgressBar.setVisibility(View.VISIBLE);
                mScrollView.setVisibility(View.GONE);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                mProgressBar.setVisibility(View.GONE);
                mScrollView.setVisibility(View.VISIBLE);

                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        ItemCategory objItem = new ItemCategory();
                        objItem.setCategoryId(objJson.getInt(Constant.CATEGORY_CID));
                        objItem.setCategoryName(objJson.getString(Constant.CATEGORY_NAME));
                        objItem.setCategoryImage(objJson.getString(Constant.CATEGORY_IMAGE));
                        mName.add(objJson.getString(Constant.CATEGORY_NAME));
                        mCategoryList.add(objItem);
                    }

                    ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(AddJobActivity.this,
                            android.R.layout.simple_spinner_dropdown_item, mName);
                    spCategory.setAdapter(areaAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }

        });
    }

    private void addJob() {
        String title = edtTitle.getText().toString();
        String designation = edtDesignation.getText().toString();
        String description = edtDescription.getText().toString();
        String salary = edtSalary.getText().toString();
        String company = edtCompanyName.getText().toString();
        String website = edtWebsite.getText().toString();
        String phone = edtPhone.getText().toString();
        String email = edtEmail.getText().toString();
        String vacancy = edtVacancy.getText().toString();
        String address = edtAddress.getText().toString();
        String qualification = edtQualification.getText().toString();
        String skill = edtSkills.getText().toString();
        String date = edtDate.getText().toString();

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("cat_id", mCategoryList.get(spCategory.getSelectedItemPosition()).getCategoryId());
        params.put("user_id", MyApp.getUserId());
        params.put("job_name", title);
        params.put("job_designation", designation);
        params.put("job_desc", description);
        params.put("job_salary", salary);
        params.put("job_company_name", company);
        params.put("job_company_website", website);
        params.put("job_phone_number", phone);
        params.put("job_mail", email);
        params.put("job_vacancy", vacancy);
        params.put("job_address", address);
        params.put("job_qualification", qualification);
        params.put("job_skill", skill);
        params.put("job_date", date);
        try {
            params.put("job_image", new File(galleryImages.get(0).getPath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        client.post(Constant.ADD_JOB_URL, params, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                super.onStart();
                showProgressDialog();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                dismissProgressDialog();
                ActivityCompat.finishAffinity(AddJobActivity.this);
                Intent intent = new Intent(getApplicationContext(), JobProviderMainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                dismissProgressDialog();
            }

        });
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

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }
}
