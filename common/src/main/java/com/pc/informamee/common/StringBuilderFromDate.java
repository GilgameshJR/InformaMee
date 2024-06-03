package com.pc.informamee.common;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StringBuilderFromDate {
    public static String BuildStringFromDate (Date ToConvert) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return dateFormat.format(ToConvert);
    }
}
        /*
        StringBuilder ConvertedBuilder=new StringBuilder(30);
        ConvertedBuilder.append(ToConvert.getDate()).append('/').append(ToConvert.getMonth()).append('/').append(ToConvert.getYear()+1900).append(' ').append(ToConvert.getHours()).append(':');
        int minutes=ToConvert.getMinutes();
        if (minutes<10)
            ConvertedBuilder.append(0);
        ConvertedBuilder.append(minutes);
        return ConvertedBuilder.toString();*/