package com.example.guardianteen;

public class HealthDataRepository {
    private static final HealthDataRepository ourInstance = new HealthDataRepository();

    private int heartRate = 0;
    private int respiratoryRate = 0;

    public static HealthDataRepository getInstance() {
        return ourInstance;
    }

    private HealthDataRepository() {}

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(int respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }
}
