package com.app.jobs;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.example.fragment.JobProviderFragment;
import com.example.util.Constant;
import com.example.util.JsonUtils;
import com.ixidev.gdpr.GDPRChecker;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.gavinliu.android.lib.shapedimageview.ShapedImageView;
import cz.msebera.android.httpclient.Header;

public class JobProviderMainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private FragmentManager fragmentManager;
    NavigationView navigationView;
    Toolbar toolbar;
    MyApplication MyApp;
    FloatingActionButton addJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        MyApp = MyApplication.getInstance();
        fragmentManager = getSupportFragmentManager();
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        addJob = (FloatingActionButton) findViewById(R.id.fab);

        JobProviderFragment homeFragment = new JobProviderFragment();
        fragmentManager.beginTransaction().replace(R.id.Container, homeFragment).commit();

        addJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(JobProviderMainActivity.this, AddJobActivity.class);
                startActivity(intent);
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                drawerLayout.closeDrawers();
                switch (item.getItemId()) {
                    case R.id.menu_go_job:
                        toolbar.setTitle(getString(R.string.job_list));
                        JobProviderFragment homeFragment = new JobProviderFragment();
                        fragmentManager.beginTransaction().replace(R.id.Container, homeFragment).commit();
                        return true;
                    case R.id.menu_go_about:
                        Intent about = new Intent(JobProviderMainActivity.this, AboutUsActivity.class);
                        startActivity(about);
                        return true;
                    case R.id.menu_go_privacy:
                        Intent privacy = new Intent(JobProviderMainActivity.this, PrivacyActivity.class);
                        startActivity(privacy);
                        return true;
                    case R.id.menu_go_profile:
                        Intent profile = new Intent(JobProviderMainActivity.this, ProfileActivity.class);
                        profile.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(profile);
                        return true;
                    case R.id.menu_go_logout:
                        Logout();
                        return true;
                    case R.id.menu_go_rate:
                        RateApp();
                        return true;
                    case R.id.menu_go_share:
                        ShareApp();
                        return true;
                }
                return false;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        if (!MyApp.getIsLogin()) {
            navigationView.getMenu().findItem(R.id.menu_go_profile).setVisible(false);
            navigationView.getMenu().findItem(R.id.menu_go_logout).setVisible(false);
        }
        setHeader();
        if (JsonUtils.isNetworkAvailable(JobProviderMainActivity.this)) {
            getAdSetting();
        }
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void Logout() {
        new AlertDialog.Builder(JobProviderMainActivity.this)
                .setTitle(getString(R.string.menu_logout))
                .setMessage(getString(R.string.logout_msg))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MyApp.saveIsLogin(false);
                        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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

    private void ShareApp() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_msg) + getPackageName());
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void RateApp() {
        final String appName = getPackageName();//your application package name i.e play store application url
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id="
                            + appName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id="
                            + appName)));
        }
    }

    private void setHeader() {
        if (MyApp.getIsLogin()) {
            View header = navigationView.getHeaderView(0);
            TextView txtHeaderName = (TextView) header.findViewById(R.id.header_name);
            TextView txtHeaderEmail = (TextView) header.findViewById(R.id.header_email);
            final ShapedImageView imageUser = (ShapedImageView) header.findViewById(R.id.header_image);
            txtHeaderName.setText(MyApp.getUserName());
            txtHeaderEmail.setText(MyApp.getUserEmail());

            AsyncHttpClient client = new AsyncHttpClient();
            client.get(Constant.USER_PROFILE_URL + MyApp.getUserId(), new AsyncHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String result = new String(responseBody);
                    try {
                        JSONObject mainJson = new JSONObject(result);
                        JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                        JSONObject objJson = jsonArray.getJSONObject(0);
                        if (!objJson.getString(Constant.USER_IMAGE).isEmpty()) {
                            Picasso.with(JobProviderMainActivity.this).load(objJson.getString(Constant.USER_IMAGE)).placeholder(R.mipmap.ic_launcher_app).into(imageUser);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                }

            });
        }
    }

    @Override
    public void onBackPressed() {
        ExitApp();
    }

    private void getAdSetting() {

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(Constant.ABOUT_URL, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String result = new String(responseBody);
                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson = jsonArray.getJSONObject(0);
                    Constant.isBanner = objJson.getBoolean("banner_ad");
                    Constant.isInterstitial = objJson.getBoolean("interstital_ad");
                    Constant.adMobBannerId = objJson.getString("banner_ad_id");
                    Constant.adMobInterstitialId = objJson.getString("interstital_ad_id");
                    Constant.adMobPublisherId = objJson.getString("publisher_id");

                    new GDPRChecker()
                            .withContext(JobProviderMainActivity.this)
                            .withPrivacyUrl(getString(R.string.privacy_url)) // your privacy url
                            .withPublisherIds(Constant.adMobPublisherId) // your admob account Publisher id
                            //  .withTestMode("9424DF76F06983D1392E609FC074596C") // remove this on real project
                            .check();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }

        });
    }

    private void ExitApp() {
        new AlertDialog.Builder(JobProviderMainActivity.this)
                .setTitle(getString(R.string.app_name))
                .setMessage(getString(R.string.exit_msg))
                .setIcon(R.mipmap.ic_launcher_app)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }
}
