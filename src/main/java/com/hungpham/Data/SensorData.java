package com.hungpham.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class SensorData {
    private String acceValue;
    private String baroValue;
    private volatile String rawData;
    private List<DataObserver> observers = new ArrayList<DataObserver>();

    public static volatile LinkedBlockingQueue<String> dataQueue;

    public SensorData() {
        acceValue = null;
        baroValue = null;
        rawData = null;
        dataQueue = new LinkedBlockingQueue<String>();
    }

    public synchronized void updateData() {
        synchronized (dataQueue) {
            try {
                rawData = dataQueue.take();
                //System.out.println("Raw data:" + rawData);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void attach(DataObserver observer) {
        observers.add(observer);
    }

    public synchronized void notifySpecificObserver(String sensor) {
        for (DataObserver observer : observers) {
            if (observer.getName().equalsIgnoreCase(sensor)) {
                observer.update();
            }
        }
    }

    public void seperateData() {
        String accel = "04FF1A1B05000000142C";
        String bar = "04FF0E1B050000000824";
        if (rawData.indexOf(accel) == 0) {
            notifySpecificObserver("acce");
        } else if (rawData.indexOf(bar) == 0) {
            notifySpecificObserver("baro");
        }
    }

    public String getData() {
        return rawData;
    }


}
