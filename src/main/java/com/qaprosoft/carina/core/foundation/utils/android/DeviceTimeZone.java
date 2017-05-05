package com.qaprosoft.carina.core.foundation.utils.android;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.qaprosoft.carina.core.foundation.utils.android.AndroidService.TimeFormat;

public class DeviceTimeZone {

    private static final Logger LOGGER = Logger.getLogger(DeviceTimeZone.class);

    private boolean auto_time;
    private boolean auto_timezone;
    private TimeFormat time_format;
    private String timezone;
    private String gmt;
    private String setDeviceDateTime;
    private boolean changeDateTime;
    private boolean refreshDeviceTime;
    private boolean daylightTime;

    public DeviceTimeZone() {
        this.auto_time = true;
        this.auto_timezone = true;
        this.time_format = TimeFormat.FORMAT_24;
        this.timezone = "";
        this.gmt = "";
        this.setDeviceDateTime = "";
        this.changeDateTime = false;
        this.refreshDeviceTime = false;
        this.daylightTime = false;
    }

    /**
     * DeviceTimeZone
     *
     * @param auto_time         boolean
     * @param auto_timezone     boolean
     * @param time_format       AndroidService.TimeFormat
     * @param timezone          String
     * @param gmt               String
     * @param setDeviceDateTime String
     * @param changeDateTime    boolean
     * @param refreshDeviceTime boolean
     */
    public DeviceTimeZone(boolean auto_time, boolean auto_timezone, TimeFormat time_format, String timezone, String gmt, String setDeviceDateTime, boolean changeDateTime, boolean refreshDeviceTime) {
        this.auto_time = auto_time;
        this.auto_timezone = auto_timezone;
        this.time_format = time_format;
        this.timezone = timezone;
        this.gmt = gmt;
        this.setDeviceDateTime = setDeviceDateTime;
        this.changeDateTime = changeDateTime;
        this.refreshDeviceTime = refreshDeviceTime;
        this.daylightTime = isDaylightTime(timezone);
    }

    /**
     * DeviceTimeZone
     *
     * @param auto_time         boolean
     * @param auto_timezone     boolean
     * @param time_format       AndroidService.TimeFormat
     * @param timezone          String
     * @param setDeviceDateTime String
     * @param changeDateTime    boolean
     * @param refreshDeviceTime boolean
     */
    public DeviceTimeZone(boolean auto_time, boolean auto_timezone, TimeFormat time_format, String timezone, String setDeviceDateTime, boolean changeDateTime, boolean refreshDeviceTime) {
        this.auto_time = auto_time;
        this.auto_timezone = auto_timezone;
        this.time_format = time_format;
        this.timezone = timezone;
        this.gmt = getTZforID();
        this.setDeviceDateTime = setDeviceDateTime;
        this.changeDateTime = changeDateTime;
        this.refreshDeviceTime = refreshDeviceTime;
        this.daylightTime = isDaylightTime(timezone);
    }


    public boolean isAutoTime() {
        return auto_time;
    }

    public void setAutoTime(boolean auto_time) {
        this.auto_time = auto_time;
    }

    public boolean isAutoTimezone() {
        return auto_timezone;
    }

    public void setAutoTimezone(boolean auto_timezone) {
        this.auto_timezone = auto_timezone;
    }

    public TimeFormat getTimeFormat() {
        return time_format;
    }

    public void setTimeFormat(TimeFormat time_format) {
        this.time_format = time_format;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getGMT() {
        if (gmt.isEmpty()) {
            gmt = getTZforID();
        }
        return gmt;
    }

    public void setGMT(String gmt) {
        this.gmt = gmt;
    }

    public String getSetDeviceDateTime() {
        return setDeviceDateTime;
    }

    public void setSetDeviceDateTime(String setDeviceDateTime) {
        this.setDeviceDateTime = setDeviceDateTime;
    }

    public boolean isChangeDateTime() {
        return changeDateTime;
    }

    public void setChangeDateTime(boolean changeDateTime) {
        this.changeDateTime = changeDateTime;
    }

    public boolean isRefreshDeviceTime() {
        return refreshDeviceTime;
    }

    public void setRefreshDeviceTime(boolean refreshDeviceTime) {
        this.refreshDeviceTime = refreshDeviceTime;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public boolean isDaylightTime() {
        return daylightTime;
    }

    public String getTZforID() {
        if (timezone.isEmpty()) return "";
        return getTimezoneOffset(DateTimeZone.forID(timezone).toTimeZone());
    }

    public static boolean isDaylightTime(String tz) {
        try {
            return DateTimeZone.forID(tz).toTimeZone().observesDaylightTime();
        } catch (Exception e) {
            LOGGER.error(e);
            return false;
        }
    }

    public static String getTimezoneOffset(String tz) {
        try {
            return getTimezoneOffset(DateTimeZone.forID(tz).toTimeZone());
        } catch (Exception e) {
            LOGGER.error(e);
            return "";
        }
    }

    public static String getTimezoneOffset(TimeZone tz) {
        Calendar cal = GregorianCalendar.getInstance(tz);
        int offsetInMillis = tz.getOffset(cal.getTimeInMillis());

        String offset = String.format("%02d:%02d", Math.abs(offsetInMillis / 3600000), Math.abs((offsetInMillis / 60000) % 60));
        offset = "GMT" + (offsetInMillis >= 0 ? "+" : "-") + offset;
        return offset;
    }


    public static boolean compareTimezoneOffsets(String timezone1, String timezone2) {

        LOGGER.info("Compare Timezone '" + timezone1 + "' and Timezone '" + timezone2 + "'.");
        if (timezone1.isEmpty() || timezone2.isEmpty()) return false;
        TimeZone tz1 = getTimezoneFromOffset(timezone1);
        TimeZone tz2 = getTimezoneFromOffset(timezone2);

        int diff = compare(tz1, tz2);
        LOGGER.info("Timezone comparison return difference: " + diff);
        return (Math.abs(diff) <= 1);

    }

    private static TimeZone getTimezoneFromOffset(String tz) {
        tz = tz.replace("GMT", "");
        String tz_p1 = tz.split(":")[0];
        String tz_p2 = tz.split(":")[1];
        if (tz_p1.startsWith("-0")) {
            tz_p1 = tz_p1.replace("-0", "-");
        }
        if (tz_p1.startsWith("+0")) {
            tz_p1 = tz_p1.replace("+0", "");
        }
        if (tz_p1.startsWith("+")) {
            tz_p1 = tz_p1.replace("+", "");
        }
        int tz_hour = Integer.parseInt(tz_p1);
        int tz_min = Integer.parseInt(tz_p2);
        return DateTimeZone.forOffsetHoursMinutes(tz_hour, tz_min).toTimeZone();
    }

    public static int compare(TimeZone tz1, TimeZone tz2) {
        Calendar cal = GregorianCalendar.getInstance(tz1);
        long date = cal.getTimeInMillis();
        return (tz2.getOffset(date) - tz1.getOffset(date)) / 3600000;
    }

    @Override
    public String toString() {
        return "DeviceTimeZone{" +
                "auto_time=" + auto_time +
                ", auto_timezone=" + auto_timezone +
                ", time_format='" + time_format + '\'' +
                ", timezone='" + timezone + '\'' +
                ", gmt='" + gmt + '\'' +
                ", setDeviceDateTime='" + setDeviceDateTime + '\'' +
                ", changeDateTime=" + changeDateTime +
                ", refreshDeviceTime=" + refreshDeviceTime +
                ", daylightTime=" + daylightTime +
                '}';
    }
}
