package org.jitsi.android.gui.util;

import java.text.SimpleDateFormat;

/**
 * Created by snt1231 on 2017/5/3.
 */

public class TimeUtils {
    public static String getNowTime(){
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String date = sDateFormat.format(new java.util.Date());
        return date;
    }
}
