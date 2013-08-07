package com.spmno.bybike;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by spmno on 13-8-7.
 */
public class LogSaver {
    private static final LogSaver instance = new LogSaver();
    final String filename = "gps_log.txt";
    FileOutputStream outputStream;
    File logFile;
    public static LogSaver getInstance() {
        return instance;
    }
    private LogSaver(){

    }

    void startLog() {
        logFile = new File(Environment.getExternalStorageDirectory(), filename);
        try {
            outputStream = new FileOutputStream(logFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    void saveLog(String log) {
        try {
            outputStream.write(log.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void stopLog() {
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
