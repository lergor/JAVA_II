package ru.ifmo.git.commands;

import java.text.*;
import java.util.Calendar;

public class Log {
    // Date:   Thu Sep 13 20:56:57 2018 +0300
    private static String currentTime() {
        DateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy ZZ");
        Calendar calendar = Calendar.getInstance();
        return df.format(calendar.getTime());
    }


}
