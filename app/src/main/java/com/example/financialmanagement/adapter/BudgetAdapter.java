package com.example.financialmanagement.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.financialmanagement.R;
import com.example.financialmanagement.tmp.BudgetDisplay;

import java.text.DecimalFormat;
import java.util.List;

public class BudgetAdapter extends BaseAdapter {

    private Context context;
    private List<BudgetDisplay> budgetList;
    private LayoutInflater inflater;

    public BudgetAdapter(Context context, List<BudgetDisplay> budgetList) {
        this.context = context;
        this.budgetList = budgetList;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return budgetList.size();
    }

    @Override
    public Object getItem(int position) {
        return budgetList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return budgetList.get(position).getSubcategoryId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_budget, parent, false);
            holder = new ViewHolder();
            holder.tvSubcategory = convertView.findViewById(R.id.tvBudgetSubcategory);
            holder.tvAmount = convertView.findViewById(R.id.tvBudgetAmount);
            holder.progressBar = convertView.findViewById(R.id.pbBudgetProgress);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BudgetDisplay item = budgetList.get(position);
        holder.tvSubcategory.setText(item.getSubcategoryName());

        DecimalFormat formatter = new DecimalFormat("#,###");
        String amountText = String.format("Đã chi: %sđ / %sđ",
                formatter.format(item.getSpentAmount()),
                formatter.format(item.getBudget()));
        holder.tvAmount.setText(amountText);

        int progress = 0;
        if (item.getBudget() > 0) {
            progress = (int) ((item.getSpentAmount() / item.getBudget()) * 100);
            if (progress > 100) progress = 100;
        }
        holder.progressBar.setProgress(progress);

        // Đổi màu progress bar theo 3 cấp độ
        int color;
        if (progress < 80) {
            color = ContextCompat.getColor(context, android.R.color.holo_green_light);
        } else if (progress < 100) {
            color = ContextCompat.getColor(context, android.R.color.holo_orange_light);
        } else {
            color = ContextCompat.getColor(context, android.R.color.holo_red_light);
        }
        holder.progressBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);

        return convertView;
    }

    static class ViewHolder {
        TextView tvSubcategory;
        TextView tvAmount;
        ProgressBar progressBar;
    }
}