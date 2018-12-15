package com.dodola.breakpad;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class MyCrashHandler implements Thread.UncaughtExceptionHandler {

    @Override
    public void uncaughtException(Thread thread, final Throwable throwable) {
        getStackTraceInfo(throwable);
    }

    public String getStackTraceInfo(final Throwable throwable) {

        String trace = "";
        try {

            Writer writer = new StringWriter();

            PrintWriter pw = new PrintWriter(writer);

            throwable.printStackTrace(pw);

            trace = writer.toString();

            pw.close();

        } catch (Exception e) {

            return "";

        }

        return trace;

    }
}



