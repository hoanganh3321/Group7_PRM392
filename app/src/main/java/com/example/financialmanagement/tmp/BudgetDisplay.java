package com.example.financialmanagement.tmp;

public class BudgetDisplay {
    private int subcategoryId;
    private String subcategoryName;
    private double budget;
    private double spentAmount;

    public BudgetDisplay(int subcategoryId, String subcategoryName, double budget, double spentAmount) {
        this.subcategoryId = subcategoryId;
        this.subcategoryName = subcategoryName;
        this.budget = budget;
        this.spentAmount = spentAmount;
    }

    public int getSubcategoryId() { return subcategoryId; }
    public String getSubcategoryName() { return subcategoryName; }
    public double getBudget() { return budget; }
    public double getSpentAmount() { return spentAmount; }

    public void setBudget(double budget) { this.budget = budget; }
    public void setSpentAmount(double spentAmount) { this.spentAmount = spentAmount; }
}
