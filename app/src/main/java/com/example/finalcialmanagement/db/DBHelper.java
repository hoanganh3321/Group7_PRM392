package com.example.finalcialmanagement.db;

import android.content.Context;

public class DBHelper {
    private static final String DATABASE_NAME = "financial_management.db";
    private static final int DATABASE_VERSION = 1;

    // Các bảng
    private static final String TABLE_USER = "user";
    private static final String TABLE_WALLET = "wallet";
    private static final String TABLE_CATEGORY = "category";
    private static final String TABLE_SUB_CATEGORY = "subcategory";
    private static final String TABLE_TRANSACTION = "transactions";
    private static final String TABLE_BUDGET = "budget";
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
}
