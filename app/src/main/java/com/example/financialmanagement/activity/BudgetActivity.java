package com.example.financialmanagement.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.financialmanagement.R;
import com.example.financialmanagement.adapter.BudgetAdapter;
import com.example.financialmanagement.db.DBHelper;
import com.example.financialmanagement.tmp.BudgetDisplay;

import java.util.ArrayList;
import java.util.List;

public class BudgetActivity extends AppCompatActivity {
    private Spinner spinnerSubcategory;
    private EditText edtBudget;
    private Button btnSaveBudget;
    private ListView lvBudget;
    private ImageButton imgbtnOverviewBudget, imgbtnTransactionBudget, imgbtnAddTransactionBudget, imgbtnAccountBudget;

    private List<BudgetDisplay> budgetList = new ArrayList<>();
    private DBHelper dbHelper;
    private int selectedSubcategoryId = -1;
    private int userId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_budget);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.budget), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Ánh xạ view
        spinnerSubcategory = findViewById(R.id.spinnerSubcategory);
        edtBudget = findViewById(R.id.edtBudget);
        btnSaveBudget = findViewById(R.id.btnSaveBudget);
        lvBudget = findViewById(R.id.lvBudget);
        imgbtnOverviewBudget = findViewById(R.id.imgbtnOverviewBudget);
        imgbtnTransactionBudget = findViewById(R.id.imgbtnTransactionBudget);
        imgbtnAddTransactionBudget = findViewById(R.id.imgbtnAddTransactionBudget);
        imgbtnAccountBudget = findViewById(R.id.imgbtnAccountBudget);

        dbHelper = new DBHelper(this);

        // Lấy userId 1 lần, tái sử dụng nhiều lần
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy userId", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadExpenseSubcategories();
        loadBudgets();

        btnSaveBudget.setOnClickListener(v -> saveBudget());

        lvBudget.setOnItemClickListener((parent, view, position, id) -> {
            BudgetDisplay selected = budgetList.get(position);
            String name = selected.getSubcategoryName();
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerSubcategory.getAdapter();
            int pos = adapter.getPosition(name);
            if (pos >= 0) spinnerSubcategory.setSelection(pos);
            edtBudget.setText(String.valueOf((int) selected.getBudget()));
            selectedSubcategoryId = selected.getSubcategoryId();
        });

        lvBudget.setOnItemLongClickListener((parent, view, position, id) -> {
            BudgetDisplay selected = budgetList.get(position);
            confirmDeleteBudget(selected.getSubcategoryId(), selected.getSubcategoryName());
            return true;
        });

        imgbtnOverviewBudget.setOnClickListener(v -> startActivity(new Intent(this, OverviewActivity.class)));
        imgbtnTransactionBudget.setOnClickListener(v -> startActivity(new Intent(this, TransactionActivity.class)));
        imgbtnAddTransactionBudget.setOnClickListener(v -> startActivity(new Intent(this, AddTransactionActivity.class)));
        imgbtnAccountBudget.setOnClickListener(v -> startActivity(new Intent(this, AccountActivity.class)));
    }

    private void loadExpenseSubcategories() {
        List<String> subcategoryNames = new ArrayList<>();

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery("SELECT name FROM subcategory WHERE categoryId = ?", new String[]{"1"})) {
            while (cursor.moveToNext()) {
                subcategoryNames.add(cursor.getString(cursor.getColumnIndexOrThrow("name")));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi tải danh mục", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subcategoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubcategory.setAdapter(adapter);
    }

    private void saveBudget() {
        String selectedSubcategory = (String) spinnerSubcategory.getSelectedItem();
        String budgetStr = edtBudget.getText().toString().trim();

        if (budgetStr.isEmpty()) {
            edtBudget.setError("Vui lòng nhập số tiền ngân sách");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(budgetStr);
            if (amount < 0) {
                edtBudget.setError("Số tiền phải lớn hơn hoặc bằng 0");
                return;
            }
        } catch (NumberFormatException e) {
            edtBudget.setError("Số tiền không hợp lệ");
            return;
        }

        try (SQLiteDatabase db = dbHelper.getWritableDatabase();
             Cursor cursor = db.rawQuery("SELECT id FROM subcategory WHERE name = ?", new String[]{selectedSubcategory})) {

            if (!cursor.moveToFirst()) {
                Toast.makeText(this, "Danh mục không tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }

            int subcategoryId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));

            try (Cursor checkCursor = db.rawQuery("SELECT id FROM budget WHERE subcategoryId = ?", new String[]{String.valueOf(subcategoryId)})) {
                if (checkCursor.moveToFirst()) {
                    db.execSQL("UPDATE budget SET budget = ? WHERE subcategoryId = ?", new Object[]{amount, subcategoryId});
                } else {
                    db.execSQL("INSERT INTO budget (subcategoryId, budget) VALUES (?, ?)", new Object[]{subcategoryId, amount});
                }
            }

            Toast.makeText(this, "Đã lưu ngân sách", Toast.LENGTH_SHORT).show();
            edtBudget.setText("");
            selectedSubcategoryId = -1;
            loadBudgets();

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi lưu ngân sách: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadBudgets() {
        budgetList.clear();

        String query = "SELECT s.id, s.name, b.budget, " +
                "(SELECT IFNULL(SUM(t.amount), 0) " +
                " FROM transactions t " +
                " JOIN wallet w ON t.walletId = w.id " +
                " WHERE t.subcategoryId = s.id AND w.userId = ? " +
                "   AND (substr(t.date, 7, 4) || '-' || substr(t.date, 4, 2)) = strftime('%Y-%m', 'now')" +
                ") AS spentAmount " +
                "FROM budget b " +
                "JOIN subcategory s ON b.subcategoryId = s.id " +
                "WHERE EXISTS (SELECT 1 FROM transactions t " +
                "              JOIN wallet w ON t.walletId = w.id " +
                "              WHERE t.subcategoryId = s.id AND w.userId = ?)";

        try (SQLiteDatabase db = dbHelper.getReadableDatabase();
             Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(userId)})) {

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                double budget = cursor.getDouble(cursor.getColumnIndexOrThrow("budget"));
                double spentAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("spentAmount"));

                budgetList.add(new BudgetDisplay(id, name, budget, spentAmount));
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi tải ngân sách: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        BudgetAdapter adapter = new BudgetAdapter(this, budgetList);
        lvBudget.setAdapter(adapter);
    }

    private void confirmDeleteBudget(int subcategoryId, String subcategoryName) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Xoá ngân sách")
                .setMessage("Bạn có chắc chắn muốn xoá ngân sách của \"" + subcategoryName + "\"?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    try (SQLiteDatabase db = dbHelper.getWritableDatabase()) {
                        db.execSQL("DELETE FROM budget WHERE subcategoryId = ?", new Object[]{subcategoryId});
                        Toast.makeText(this, "Đã xoá ngân sách", Toast.LENGTH_SHORT).show();
                        edtBudget.setText("");
                        selectedSubcategoryId = -1;
                        loadBudgets();
                    } catch (Exception e) {
                        Toast.makeText(this, "Lỗi xoá ngân sách: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }
}