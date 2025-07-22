package com.example.financialmanagement.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.financialmanagement.R;
import com.example.financialmanagement.adapter.TransactionListAdapter;
import com.example.financialmanagement.db.DBHelper;
import com.example.financialmanagement.model.Transaction;
import com.example.financialmanagement.tmp.TransactionDisplay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TransactionActivity extends AppCompatActivity {
    private TextView tvTotalBalanceTransaction;
    private RadioGroup rgTime, rgType;
    private RadioButton rdLastMonth, rdThisMonth, rdOption, rdIncome, rdExpense, rdAll;
    private ListView lvTransaction;
    private ImageButton imgbtnOverviewTransaction, imgbtnAddTransactionTransaction, imgbtnBudgetTransaction, imgbtnAccountTransaction;

    private String selectedStartDate = null;
    private final List<TransactionDisplay> transactionList = new ArrayList<>();
    private int userId;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        initViews();
        dbHelper = new DBHelper(this);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);
        if (userId == -1) return;

        rgTime.setOnCheckedChangeListener((group, checkedId) -> loadFilteredTransactions());
        rgType.setOnCheckedChangeListener((group, checkedId) -> loadFilteredTransactions());

        rdOption.setOnClickListener(v -> showDatePicker());

        setBottomNavListeners();

        loadTotalBalance();
        loadFilteredTransactions();
    }

    private void initViews() {
        tvTotalBalanceTransaction = findViewById(R.id.tvTotalBalanceTransaction);
        rgTime = findViewById(R.id.rgTime);
        rgType = findViewById(R.id.rgType);
        rdLastMonth = findViewById(R.id.rdLastMonth);
        rdThisMonth = findViewById(R.id.rdThisMonth);
        rdOption = findViewById(R.id.rdOption);
        rdIncome = findViewById(R.id.rdIncome);
        rdExpense = findViewById(R.id.rdExpense);
        rdAll = findViewById(R.id.rdAll);
        lvTransaction = findViewById(R.id.lvTransaction);
        imgbtnOverviewTransaction = findViewById(R.id.imgbtnOverviewTransaction);
        imgbtnAddTransactionTransaction = findViewById(R.id.imgbtnAddTransactionTransaction);
        imgbtnBudgetTransaction = findViewById(R.id.imgbtnBudgetTransaction);
        imgbtnAccountTransaction = findViewById(R.id.imgbtnAccountTransaction);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            selectedStartDate = String.format("%04d-%02d-%02d", year, month + 1, day);
            loadFilteredTransactions();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void setBottomNavListeners() {
        imgbtnOverviewTransaction.setOnClickListener(v -> startActivity(new Intent(this, OverviewActivity.class)));
        imgbtnAddTransactionTransaction.setOnClickListener(v -> startActivity(new Intent(this, AddTransactionActivity.class)));
        imgbtnBudgetTransaction.setOnClickListener(v -> startActivity(new Intent(this, BudgetActivity.class)));
        imgbtnAccountTransaction.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
    }

    private void loadTotalBalance() {
        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT SUM(balance) FROM wallet WHERE userId = ?", new String[]{String.valueOf(userId)})) {

            if (cursor.moveToFirst()) {
                double totalBalance = cursor.getDouble(0);
                tvTotalBalanceTransaction.setText(String.format("%,.0f đ", totalBalance));
            }
        }
    }

    private void loadFilteredTransactions() {
        int timeId = rgTime.getCheckedRadioButtonId();
        int typeId = rgType.getCheckedRadioButtonId();

        StringBuilder query = new StringBuilder(
                "SELECT t.*, s.name AS subcategoryName, s.categoryId " +
                        "FROM transactions t JOIN subcategory s ON t.subcategoryId = s.id " +
                        "JOIN wallet w ON t.walletId = w.id WHERE w.userId = ?"
        );

        List<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));

        if (timeId == R.id.rdThisMonth || timeId == R.id.rdLastMonth) {
            Calendar cal = Calendar.getInstance();
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            if (timeId == R.id.rdLastMonth) {
                month--;
                if (month == 0) {
                    month = 12;
                    year--;
                }
            }
            query.append(" AND substr(t.date, 4, 2) = ? AND substr(t.date, 7, 4) = ?");
            args.add(String.format("%02d", month));
            args.add(String.valueOf(year));
        } else if (timeId == R.id.rdOption && selectedStartDate != null) {
            String formatted = selectedStartDate.replace("-", "");
            query.append(" AND (substr(t.date,7,4) || substr(t.date,4,2) || substr(t.date,1,2)) >= ?");
            args.add(formatted);
        }

        if (typeId == R.id.rdIncome) {
            query.append(" AND s.categoryId = 2");
        } else if (typeId == R.id.rdExpense) {
            query.append(" AND s.categoryId = 1");
        }

        query.append(" ORDER BY substr(t.date, 7, 4) || '-' || substr(t.date, 4, 2) || '-' || substr(t.date, 1, 2) DESC");

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(query.toString(), args.toArray(new String[0]))) {

            transactionList.clear();
            while (cursor.moveToNext()) {
                Transaction t = new Transaction(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("note")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("walletId")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("subcategoryId"))
                );
                transactionList.add(new TransactionDisplay(t,
                        cursor.getString(cursor.getColumnIndexOrThrow("subcategoryName")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("categoryId"))));
            }
        }

        TransactionListAdapter adapter = new TransactionListAdapter(this, transactionList);
        lvTransaction.setAdapter(adapter);

        lvTransaction.setOnItemClickListener((parent, view, position, id) -> showTransactionDetail(transactionList.get(position)));
        lvTransaction.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmDelete(transactionList.get(position).getTransaction().getId());
            return true;
        });
    }

    private void showTransactionDetail(TransactionDisplay item) {
        Transaction t = item.getTransaction();
        String msg = "Tên giao dịch: " + t.getName() +
                "\n\nSố tiền: " + String.format("%,.0f đ", t.getAmount()) +
                "\n\nNgày: " + t.getDate() +
                "\n\nGhi chú: " + t.getNote() +
                "\n\nDanh mục: " + item.getSubcategoryName() +
                "\n\nLoại: " + (item.getCategoryId() == 1 ? "Chi tiêu" : "Thu nhập");

        new AlertDialog.Builder(this)
                .setTitle("Chi tiết giao dịch")
                .setMessage(msg)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private void confirmDelete(int transactionId) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa giao dịch này?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteTransaction(transactionId))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteTransaction(int transactionId) {
        try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
            try (Cursor cursor = db.rawQuery(
                    "SELECT t.amount, t.walletId, s.categoryId FROM transactions t " +
                            "JOIN subcategory s ON t.subcategoryId = s.id WHERE t.id = ?",
                    new String[]{String.valueOf(transactionId)})) {

                if (cursor.moveToFirst()) {
                    double amount = cursor.getDouble(0);
                    int walletId = cursor.getInt(1);
                    int categoryId = cursor.getInt(2);
                    String sql = categoryId == 1
                            ? "UPDATE wallet SET balance = balance + ? WHERE id = ?"
                            : "UPDATE wallet SET balance = balance - ? WHERE id = ?";
                    db.execSQL(sql, new Object[]{amount, walletId});
                }
            }

            int rows = db.delete("transactions", "id = ?", new String[]{String.valueOf(transactionId)});
            if (rows > 0) {
                loadTotalBalance();
                loadFilteredTransactions();
            }
        }
    }
}