package com.example.financialmanagement.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.financialmanagement.db.DBHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DBHelper dbHelper = new DBHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId == -1) {
            // Nếu không có userId thì không gửi thông báo
            return;
        }

        String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().getTime());

        // Truy vấn thêm điều kiện userId
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM transactions " +
                        "JOIN wallet ON transactions.walletId = wallet.id " +
                        "WHERE transactions.date = ? AND wallet.userId = ?",
                new String[]{today, String.valueOf(userId)}
        );


        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count == 0) {
                NotificationHelper.showNotification(context, "Nhắc nhở", "Bạn chưa nhập giao dịch nào hôm nay.");
            }
        }

        cursor.close();
        db.close();

        // Lên lịch lại cho ngày hôm sau
        NotificationScheduler.scheduleDailyNotification(context);
    }
}