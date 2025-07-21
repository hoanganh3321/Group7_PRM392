package com.example.financialmanagement.model;

public class Budget {
    private int id;
    private int subcategoryId;
    private double budget;

    public Budget(int id, int subcategoryId, double budget) {
        this.id = id;
        this.subcategoryId = subcategoryId;
        this.budget = budget;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(int subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public double getBudget() {
        return budget;
    }

    public void setBudget(double budget) {
        this.budget = budget;
    }
}
