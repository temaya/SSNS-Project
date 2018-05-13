package com.hungpham.Controller;

import com.hungpham.Data.SensorData;
import com.hungpham.Utils.Utils;
import gnu.io.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.TooManyListenersException;

public class SerialPortController implements Runnable, SerialPortEventListener {
    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;
    private static CommPortIdentifier portID;

    public static volatile int mode = 0;

    private Thread writeThread;

    private String data;

    public SerialPortController() {
        data = null;
        init();
    }

    private void init() {
        try {
            portID = CommPortIdentifier.getPortIdentifier("COM3");
            System.out.println("Serial port: " + portID.getName());
        } catch (NoSuchPortException e) {
            e.printStackTrace();
        }
        try {
            serialPort = (SerialPort) portID.open("SerialPortController", 2000);
            inputStream = serialPort.getInputStream();
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);
            serialPort.notifyOnBreakInterrupt(true);
            serialPort.notifyOnCarrierDetect(true);
            serialPort.notifyOnCTS(true);
            serialPort.notifyOnDataAvailable(true);
            serialPort.notifyOnDSR(true);
            serialPort.notifyOnFramingError(true);
            serialPort.notifyOnOutputEmpty(true);
            serialPort.notifyOnOverrunError(true);
            serialPort.notifyOnParityError(true);
            serialPort.notifyOnRingIndicator(true);
            serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (PortInUseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
        writeThread = new Thread(this);
        writeThread.start();
    }

    public void initWrite() {
        try {
            outputStream = serialPort.getOutputStream();
        } catch (IOException e) {
        }

        try {
            serialPort.notifyOnOutputEmpty(true);
        } catch (Exception e) {
            System.out.println("Error setting event notification");
            System.out.println(e.toString());
            System.exit(-1);
        }
    }

    public void write(String message) {
        byte[] bytes = Utils.stringToHexBytes(message);
        System.out.println("Writing \"" + message + "\" to " + serialPort.getName());
        try {
            outputStream.write(bytes);
        } catch (IOException e) {
        }
    }

    public synchronized void serialEvent(SerialPortEvent event) {
        if (event.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            //System.out.println("Data available: ");
            byte[] readBuffer = new byte[32];
            try {
                while (inputStream.available() > 0) {
                    int numBytes = inputStream.read(readBuffer);
                }
                data = Utils.bytesToHexString(readBuffer);
                if (mode == 1) {
                    SensorData.dataQueue.add(data);
                }
                //System.out.println(data);
            } catch (IOException ioe) {
                System.out.println("Exception " + ioe);
            }
        }
    }

    private void executeControlHex(String path) {
        ArrayList<String> strings = Utils.readHexFromFile(path);
        for (String s : strings) {
            write(s);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void run() {
        initWrite();
        executeControlHex("autoConnectHex.txt");
        executeControlHex("runSensor");
        mode = 1;
        while (true) {
            if (UI.command.equalsIgnoreCase("stop")) {
                mode = 0;
                break;
            }
        }
        executeControlHex("autoDisconnectHex");
        System.exit(0);
    }

}
