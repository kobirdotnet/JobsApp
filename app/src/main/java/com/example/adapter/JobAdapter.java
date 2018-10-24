package com.example.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.jobs.JobDetailsActivity;
import com.app.jobs.MyApplication;
import com.app.jobs.R;
import com.example.item.ItemJob;
import com.example.util.Constant;
import com.example.util.JsonUtils;
import com.example.util.PopUpAds;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.ItemRowHolder> {

    private ArrayList<ItemJob> dataList;
    private Context mContext;
    MyApplication MyApp;

    public JobAdapter(Context context, ArrayList<ItemJob> dataList) {
        this.dataList = dataList;
        this.mContext = context;
        MyApp = MyApplication.getInstance();
    }

    @NonNull
    @Override
    public ItemRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list_job, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemRowHolder holder, final int position) {
        final ItemJob singleItem = dataList.get(position);
        holder.jobTitle.setText(singleItem.getJobName());
        holder.companyTitle.setText(singleItem.getJobCompanyName());
        String date = mContext.getString(R.string.date_posted) + singleItem.getJobDate();
        String designation = mContext.getString(R.string.designation) + singleItem.getJobDesignation();

        holder.jobDate.setText(date);
        holder.jobDesignation.setText(designation);
        holder.jobAddress.setText(singleItem.getJobAddress());

        Picasso.with(mContext).load(singleItem.getJobImage()).placeholder(R.drawable.placeholder).into(holder.image);
        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpAds.ShowInterstitialAds(mContext);
                Intent intent = new Intent(mContext, JobDetailsActivity.class);
                intent.putExtra("Id", singleItem.getId());
                mContext.startActivity(intent);
            }
        });

        holder.btnApplyJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyApp.getIsLogin()) {
                    new ApplyJob().execute(Constant.APPLY_JOB_URL + MyApp.getUserId() + "&job_id=" + singleItem.getId());
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.need_login), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public class ItemRowHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView jobTitle, companyTitle, jobDate, jobDesignation, jobAddress;
        LinearLayout lyt_parent;
        Button btnApplyJob;

        public ItemRowHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            jobTitle = (TextView) itemView.findViewById(R.id.text_job_title);
            companyTitle = (TextView) itemView.findViewById(R.id.text_job_company);
            jobDate = (TextView) itemView.findViewById(R.id.text_job_date);
            jobDesignation = (TextView) itemView.findViewById(R.id.text_job_designation);
            jobAddress = (TextView) itemView.findViewById(R.id.text_job_address);
            lyt_parent = (LinearLayout) itemView.findViewById(R.id.rootLayout);
            btnApplyJob = (Button) itemView.findViewById(R.id.btn_apply_job);
        }
    }

    private class ApplyJob extends AsyncTask<String, Void, String> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage("Loading...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            return JsonUtils.getJSONString(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (null != pDialog && pDialog.isShowing()) {
                pDialog.dismiss();
            }

            if (null == result || result.length() == 0) {
                showToast(mContext.getString(R.string.nodata));
            } else {

                try {
                    JSONObject mainJson = new JSONObject(result);
                    JSONArray jsonArray = mainJson.getJSONArray(Constant.ARRAY_NAME);
                    JSONObject objJson;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        objJson = jsonArray.getJSONObject(i);
                        showToast(objJson.getString(Constant.MSG));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
