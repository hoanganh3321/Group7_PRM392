package com.example.financialmanagement.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "financial_management.db";
    private static final int DATABASE_VERSION = 1;

    // Các bảng
    private static final String TABLE_USER = "user";
    private static final String TABLE_WALLET = "wallet";
    private static final String TABLE_CATEGORY = "category";
    private static final String TABLE_SUB_CATEGORY = "subcategory";
    private static final String TABLE_TRANSACTION = "transactions";
    private static final String TABLE_BUDGET = "budget";


    // Câu lệnh tạo bảng
    private static final String CREATE_USER_TABLE =
            "CREATE TABLE " + TABLE_USER + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT," +
                    "password TEXT)";

    private static final String CREATE_WALLET_TABLE =
            "CREATE TABLE " + TABLE_WALLET + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "balance REAL," +
                    "userId INTEGER," +
                    "FOREIGN KEY (userId) REFERENCES " + TABLE_USER + "(id) ON DELETE CASCADE)";

    private static final String CREATE_CATEGORY_TABLE =
            "CREATE TABLE " + TABLE_CATEGORY + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT)";

    private static final String CREATE_SUB_CATEGORY_TABLE =
            "CREATE TABLE " + TABLE_SUB_CATEGORY + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "categoryId INTEGER," +
                    "FOREIGN KEY (categoryId) REFERENCES " + TABLE_CATEGORY + "(id) ON DELETE CASCADE)";

    private static final String CREATE_BUDGET_TABLE =
            "CREATE TABLE " + TABLE_BUDGET + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "subcategoryId INTEGER," +
                    "budget REAL," +
                    "FOREIGN KEY (subcategoryId) REFERENCES subcategory(id) ON DELETE CASCADE)";

    private static final String CREATE_TRANSACTION_TABLE =
            "CREATE TABLE " + TABLE_TRANSACTION + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "amount REAL," +
                    "note TEXT," +
                    "date TEXT," +
                    "walletId INTEGER," +
                    "subcategoryId INTEGER," +
                    "FOREIGN KEY (walletId) REFERENCES " + TABLE_WALLET + "(id) ON DELETE CASCADE," +
                    "FOREIGN KEY (subcategoryId) REFERENCES " + TABLE_SUB_CATEGORY + "(id) ON DELETE CASCADE)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_WALLET_TABLE);
        db.execSQL(CREATE_CATEGORY_TABLE);
        db.execSQL(CREATE_SUB_CATEGORY_TABLE);
        db.execSQL(CREATE_TRANSACTION_TABLE);
        db.execSQL(CREATE_BUDGET_TABLE);
        db.execSQL("PRAGMA foreign_keys=ON;");

        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WALLET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SUB_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        onCreate(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Dử liệu thử
        // User
        db.execSQL("INSERT INTO user (username, password) VALUES ('admin', '12345678');");
        // Wallets (userId = 1)
        db.execSQL("INSERT INTO wallet (name, balance, userId) VALUES ('Tiền mặt', 500000, 1);"); // id = 1
        db.execSQL("INSERT INTO wallet (name, balance, userId) VALUES ('Ngân hàng', 10000000, 1);"); // id = 2
        // Categories
        db.execSQL("INSERT INTO category (name) VALUES ('Chi tiêu');");  // id = 1
        db.execSQL("INSERT INTO category (name) VALUES ('Thu nhập');");  // id = 2
        // Sub-categories (2 mỗi category)
        db.execSQL("INSERT INTO subcategory (name, categoryId) VALUES ('Ăn uống', 1);");   // id = 1
        db.execSQL("INSERT INTO subcategory (name, categoryId) VALUES ('Đi lại', 1);");    // id = 2
        db.execSQL("INSERT INTO subcategory (name, categoryId) VALUES ('Lương', 2);");     // id = 3
        db.execSQL("INSERT INTO subcategory (name, categoryId) VALUES ('Thưởng', 2);");    // id = 4
        // Transactions (5 chi tiêu, 5 thu nhập)
        db.execSQL("INSERT INTO transactions (name, amount, note, date, walletId, subcategoryId) VALUES ('Ăn sáng', 30000, 'Phở bò', '14/05/2025', 1, 1);");
        db.execSQL("INSERT INTO transactions (name, amount, note, date, walletId, subcategoryId) VALUES ('Ăn trưa', 50000, 'Cơm tấm', '14/05/2025', 1, 1);");
        db.execSQL("INSERT INTO transactions (name, amount, note, date, walletId, subcategoryId) VALUES ('Grab', 40000, 'Đi làm', '13/05/2025', 1, 2);");
        db.execSQL("INSERT INTO transactions (name, amount, note, date, walletId, subcategoryId) VALUES ('Xe buýt', 7000, 'Đi học', '12/05/2025', 1, 2);");
        db.execSQL("INSERT INTO transactions (name, amount, note, date, walletId, subcategoryId) VALUES ('Ăn vặt', 20000, 'Trà sữa', '11/05/2025', 1, 1);");

        db.execSQL("INSERT INTO transactions (name, amount, note, date, walletId, subcategoryId) VALUES ('Lương tháng 5', 15000000, 'Công ty ABC', '01/05/2025', 2, 3);");
        db.execSQL("INSERT INTO transactions (name, amount, note, date, walletId, subcategoryId) VALUES ('Lương freelance', 5000000, 'Dự án web', '03/05/2025', 2, 3);");
        db.execSQL("INSERT INTO transactions (name, amount, note, date, walletId, subcategoryId) VALUES ('Thưởng hiệu suất', 2000000, 'Tháng 4', '05/05/2025', 2, 4);");
        db.execSQL("INSERT INTO transactions (name, amount, note, date, walletId, subcategoryId) VALUES ('Thưởng lễ', 1000000, '30/4-1/5', '06/05/2025', 2, 4);");
        db.execSQL("INSERT INTO transactions (name, amount, note, date, walletId, subcategoryId) VALUES ('Lương part-time', 3000000, 'Trợ giảng', '07/05/2025', 2, 3);");
    }

}
