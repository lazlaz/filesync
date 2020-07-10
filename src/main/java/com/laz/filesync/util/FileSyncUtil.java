package com.laz.filesync.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Calendar;

public class FileSyncUtil {
	/**
	 * 获取时间字符串
	 * @return
	 */
    public static String getTimeStr(){
    	LocalDate d = LocalDate.now();
        int year = d.getYear();
        int month = d.getMonthValue();
        int date = d.getDayOfMonth();
        
        LocalTime time = LocalTime.now();
        int hour = time.getHour();
        int minute = time.getMinute();
        int secord = time.getSecond();
        return year+"-"+month+"-"+ date +"-" +hour+ "-" + minute+"-"+secord;
    }
    public static void main(String[] args) {
		System.out.println(getTimeStr());
	}
}
