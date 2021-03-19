/*******************************************************************************
 * Copyright 2013-2020 QaProSoft (http://www.qaprosoft.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.qaprosoft.carina.core.foundation.utils.android;

import java.lang.invoke.MethodHandles;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceTimeZone {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private boolean auto_time;
    private boolean auto_timezone;
    private TimeFormat time_format;
    private String timezone;
    private String gmt;
    private String setDeviceDateTime;
    private boolean changeDateTime;
    private boolean refreshDeviceTime;
    private boolean daylightTime;

    public enum TimeFormat {
        FORMAT_12("12"),
        FORMAT_24("24");
        private String format;

        TimeFormat(String format) {
            this.format = format;
        }

        public String format() {
            return format;
        }

        public String toString() {
            return format;
        }

        public static TimeFormat parse(String text) {
            if (text != null) {
                for (TimeFormat type : TimeFormat.values()) {
                    if (text.equalsIgnoreCase(type.toString())) {
                        return type;
                    }
                }
            }
            return FORMAT_12;
        }
    }

    public enum TimeZoneFormat {
        BUENOS_AIRES("America/Buenos_Aires", "Buenos Aires", "GMT-03:00", "ART"),
        ST_JOHN("America/St_Johns", "St. John", "GMT-03:30", "NST"),
        HALIFAX("America/Halifax", "Halifax", "GMT-03:00", "AST"),
        BARBADOS("America/Barbados", "Barbados", "GMT-04:00", ""),
        EASTERN("America/New_York", "Eastern", "GMT-04:00", "EST"),
        CENTRAL("America/Chicago", "Central", "GMT-05:00", "CST"),
        BOGOTA("America/Bogota", "Bogota", "GMT-05:00", ""),
        CHIHUAHUA("America/Chihuahua", "Chihuahua", "GMT-06:00", ""),
        MOUNTAIN("America/Phoenix", "Phoenix,Mountain", "GMT-07:00", "MST"),
        PACIFIC("America/Los_Angeles", "Pacific", "GMT-08:00", "PST"),
        ALASKA("America/Anchorage", "Alaska", "GMT-09:00", "AKST"),
        HAWAII("Pacific/Honolulu", "Hawaii", "GMT-10:00", ""),
        SYDNEY("Australia/Sydney", "Sydney", "GMT+10:00", ""),
        SEOUL("Asia/Seoul", "Seoul", "GMT+09:00", ""),
        TAIPEI("Asia/Taipei", "Taipei", "GMT+08:00", ""),
        PERTH("Australia/Perth", "Perth", "GMT+08:00", ""),
        MINSK("Europe/Minsk", "Minsk", "GMT+03:00", "MSQ"),
        JERUSALEM("Asia/Jerusalem", "Jerusalem", "GMT+02:00", "IST"),
        EUROPE("Europe/Amsterdam", "Amsterdam", "GMT+01:00", "CET"),
        GMT("Europe/London", "London", "GMT+00:00", "GMT");
        private String timeZone;
        private String settingsTZ;
        private String gmtTZ;
        private String abbr;

        TimeZoneFormat(String timeZone, String settingsTZ, String gmtTZ, String abbr) {
            this.timeZone = timeZone;
            this.settingsTZ = settingsTZ;
            this.gmtTZ = gmtTZ;
            this.abbr = abbr;
        }

        public String getTimeZone() {
            return timeZone;
        }

        public String getSettingsTZ() {
            return settingsTZ;
        }

        public String getGMT() {
            return gmtTZ;
        }

        public String getAbbr() {
            return abbr;
        }

        public static TimeZoneFormat parse(String text) {
            if (text != null) {
                for (TimeZoneFormat type : TimeZoneFormat.values()) {
                    if (type.getSettingsTZ().equalsIgnoreCase(text) || type.getTimeZone().toLowerCase().contains(text.toLowerCase())
                            || type.getGMT().equalsIgnoreCase(text) || type.getAbbr().equals(text)) {
                        return type;
                    }
                }
            }
            return GMT;
        }

        public String toString() {
            return "TimeZoneFormat{" + "timeZone='" + timeZone + '\'' + ", settingsTZ='" + settingsTZ + '\'' + ", gmtTZ='" + gmtTZ + '\'' + ", abbr='"
                    + abbr + '\'' + '}';
        }
    }

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
     * @param auto_time boolean
     * @param auto_timezone boolean
     * @param time_format AndroidService.TimeFormat
     * @param timezone String
     * @param gmt String
     * @param setDeviceDateTime String
     * @param changeDateTime boolean
     * @param refreshDeviceTime boolean
     */
    public DeviceTimeZone(boolean auto_time, boolean auto_timezone, TimeFormat time_format, String timezone, String gmt, String setDeviceDateTime,
            boolean changeDateTime, boolean refreshDeviceTime) {
        this.auto_time = auto_time;
        this.auto_timezone = auto_timezone;
        this.time_format = time_format;
        this.timezone = timezone;
        if (gmt.isEmpty()) {
            this.gmt = getTZforID();
        } else {
            this.gmt = gmt;
        }
        this.setDeviceDateTime = setDeviceDateTime;
        this.changeDateTime = changeDateTime;
        this.refreshDeviceTime = refreshDeviceTime;
        this.daylightTime = isDaylightTime(timezone);
    }

    /**
     * DeviceTimeZone
     *
     * @param auto_time boolean
     * @param auto_timezone boolean
     * @param time_format AndroidService.TimeFormat
     * @param timezone String
     * @param setDeviceDateTime String
     * @param changeDateTime boolean
     * @param refreshDeviceTime boolean
     */
    public DeviceTimeZone(boolean auto_time, boolean auto_timezone, TimeFormat time_format, String timezone, String setDeviceDateTime,
            boolean changeDateTime, boolean refreshDeviceTime) {
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

    public boolean isDaylightTime() {
        return daylightTime;
    }

    public String getTZforID() {
        if (timezone.isEmpty())
            return "";
        return getTimezoneOffset(DateTimeZone.forID(timezone).toTimeZone());
    }

    public static boolean isDaylightTime(String tz) {
        try {
            return DateTimeZone.forID(tz).toTimeZone().observesDaylightTime();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    public static String getTimezoneOffset(String tz) {
        try {
            return getTimezoneOffset(DateTimeZone.forID(tz).toTimeZone());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
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
        if (timezone1.isEmpty() || timezone2.isEmpty())
            return false;
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
                ", time_format=" + time_format +
                ", timezone='" + timezone + '\'' +
                ", gmt='" + gmt + '\'' +
                ", setDeviceDateTime='" + setDeviceDateTime + '\'' +
                ", changeDateTime=" + changeDateTime +
                ", refreshDeviceTime=" + refreshDeviceTime +
                ", daylightTime=" + daylightTime +
                '}';
    }
}
