package com.example.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.jobs.EditJobActivity;
import com.app.jobs.MyApplication;
import com.app.jobs.R;
import com.app.jobs.UserListActivity;
import com.example.item.ItemJob;
import com.example.util.Constant;
import com.example.util.PopUpAds;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class JobProviderAdapter extends RecyclerView.Adapter<JobProviderAdapter.ItemRowHolder> {

    private ArrayList<ItemJob> dataList;
    private Context mContext;
    MyApplication MyApp;

    public JobProviderAdapter(Context context, ArrayList<ItemJob> dataList) {
        this.dataList = dataList;
        this.mContext = context;
        MyApp = MyApplication.getInstance();
    }

    @NonNull
    @Override
    public ItemRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list_job_provider, parent, false);
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
        String total = mContext.getString(R.string.total_job);
        holder.btnApplyJob.setText(String.format(total, singleItem.getJobApplyTotal()));
        Picasso.with(mContext).load(singleItem.getJobImage()).placeholder(R.drawable.placeholder).into(holder.image);
        holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpAds.ShowInterstitialAds(mContext);
                Intent intent = new Intent(mContext, EditJobActivity.class);
                intent.putExtra("Id", singleItem.getId());
                mContext.startActivity(intent);
            }
        });

        holder.btnApplyJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserListActivity.class);
                intent.putExtra("Id", singleItem.getId());
                mContext.startActivity(intent);
            }
        });

        holder.btnDeleteJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteJob(position, singleItem.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public class ItemRowHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        TextView jobTitle, companyTitle, jobDate, jobDesignation, jobAddress;
        LinearLayout lyt_parent;
        Button btnApplyJob, btnDeleteJob;

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
            btnDeleteJob = (Button) itemView.findViewById(R.id.btn_delete_job);
        }
    }

    private void deleteJob(final int position, final String id) {
        new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.delete_this_job))
                .setMessage(mContext.getString(R.string.delete_job_confirm))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        AsyncHttpClient client = new AsyncHttpClient();
                        RequestParams params = new RequestParams();
                        params.put("job_id", id);
                        client.get(Constant.USER_JOB_DELETE, params, new AsyncHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                dataList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, dataList.size());
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            }

                        });
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
