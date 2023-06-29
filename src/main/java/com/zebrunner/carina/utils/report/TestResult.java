package com.zebrunner.carina.utils.report;

public class TestResult {

    private TestResultType testResultType;
    private int amountOfPassed;
    private int amountOfFailed;
    private int amountOfSkipped;

    public TestResult(TestResultType testResultType) {
        this.testResultType = testResultType;
    }

    public TestResult(TestResultType testResultType, int amountOfPassed, int amountOfFailed, int amountOfSkipped) {
        this.testResultType = testResultType;
        this.amountOfPassed = amountOfPassed;
        this.amountOfFailed = amountOfFailed;
        this.amountOfSkipped = amountOfSkipped;
    }

    public TestResultType getTestResultType() {
        return testResultType;
    }

    public void setTestResultType(TestResultType testResultType) {
        this.testResultType = testResultType;
    }

    public int getAmountOfPassed() {
        return amountOfPassed;
    }

    public void setAmountOfPassed(int amountOfPassed) {
        this.amountOfPassed = amountOfPassed;
    }

    public int getAmountOfFailed() {
        return amountOfFailed;
    }

    public void setAmountOfFailed(int amountOfFailed) {
        this.amountOfFailed = amountOfFailed;
    }

    public int getAmountOfSkipped() {
        return amountOfSkipped;
    }

    public void setAmountOfSkipped(int amountOfSkipped) {
        this.amountOfSkipped = amountOfSkipped;
    }
}
