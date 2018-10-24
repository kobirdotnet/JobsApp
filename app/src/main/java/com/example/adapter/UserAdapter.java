package com.example.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.jobs.R;
import com.example.item.ItemUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ItemRowHolder> {

    private ArrayList<ItemUser> dataList;
    private Context mContext;

    public UserAdapter(Context context, ArrayList<ItemUser> dataList) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ItemRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_list_user, parent, false);
        return new ItemRowHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemRowHolder holder, final int position) {
        final ItemUser singleItem = dataList.get(position);
        holder.textName.setText(singleItem.getUserName());
        holder.textEmail.setText(singleItem.getUserEmail());
        holder.textPhone.setText(singleItem.getUserPhone());
        if (!singleItem.getUserCity().isEmpty()) {
            holder.textCity.setText(singleItem.getUserCity());
        } else {
            holder.textCity.setVisibility(View.GONE);
        }
        if (!singleItem.getUserImage().isEmpty()) {
            Picasso.with(mContext).load(singleItem.getUserImage()).placeholder(R.mipmap.ic_launcher_app).into(holder.image);
        }

        if (singleItem.getUserResume().isEmpty()) {
            holder.btnResume.setVisibility(View.GONE);
        }

        holder.textEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto", singleItem.getUserEmail(), null));
                emailIntent
                        .putExtra(Intent.EXTRA_SUBJECT, "Reply for the post ");
                mContext.startActivity(Intent.createChooser(emailIntent, "Send suggestion..."));
            }
        });

        holder.textPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", singleItem.getUserPhone(), null));
                mContext.startActivity(intent);
            }
        });

        holder.btnResume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(singleItem.getUserResume()));
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return (null != dataList ? dataList.size() : 0);
    }

    public class ItemRowHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView textName, textEmail, textPhone, textCity;
        LinearLayout lyt_parent;
        Button btnResume;

        public ItemRowHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            textName = (TextView) itemView.findViewById(R.id.text_job_title);
            textEmail = (TextView) itemView.findViewById(R.id.text_email);
            textPhone = (TextView) itemView.findViewById(R.id.text_phone);
            textCity = (TextView) itemView.findViewById(R.id.text_city);
            lyt_parent = (LinearLayout) itemView.findViewById(R.id.rootLayout);
            btnResume = (Button) itemView.findViewById(R.id.btn_show_resume);
        }
    }
}
