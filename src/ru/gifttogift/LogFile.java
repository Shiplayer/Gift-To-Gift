package ru.gifttogift;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Date;

public class LogFile {

    private final PrintWriter pw;

    public LogFile() throws FileNotFoundException {
        pw = new PrintWriter(new FileOutputStream(new File("log.txt"), true));
    }

    public void write(Exception e){
        synchronized (pw) {
            pw.write(new Date().toString() + " ");
            pw.println(e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) {
                pw.println(s.toString());
            }
            pw.flush();
        }
    }
    public void writeInfo(String s){
        synchronized (pw) {
            pw.println("info: " + s);
            pw.flush();
        }
    }
}
