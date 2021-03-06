package com.hungpham.UI;

import com.hungpham.Controller.Definitions;

import java.util.concurrent.Executors;

public class AcceGraph extends RTGraph {
    private AddToQueue addToQueue;

    public AcceGraph() {
        init("Accelerometer Data", -1, 9, 0.5);
    }

    private class AddToQueue implements Runnable {
        public void run() {
            // add a item of random data to queue
            String acce = utils.TCPReceive(Definitions.GRAPH_ACCE_PORT);
            dataQ.add(Double.parseDouble(acce));
            executor.execute(this);
        }
    }

    @Override
    public void executeGraph() {
        //-- Prepare Executor Services
        executor = Executors.newCachedThreadPool();
        addToQueue = new AddToQueue();
        executor.execute(addToQueue);
        //-- Prepare Timeline
        prepareTimeline();
    }
}
