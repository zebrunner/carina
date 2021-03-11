package com.qaprosoft.carina.core.utils;

import com.qaprosoft.carina.core.foundation.utils.DateUtils;
import com.qaprosoft.carina.core.foundation.utils.R;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtilsTest {

    private static final String DATE_FORMAT = R.CONFIG.get("date_format");
    private static final String TIME_FORMAT = R.CONFIG.get("time_format");

    private static final String START_DATE = "05:35:15 2021-01-01";

    @Test
    public void testValidCurrentDate() {
        String currentDateStr = DateUtils.now();

        Assert.assertTrue(isDateValid(currentDateStr, DATE_FORMAT), currentDateStr + " has invalid date format");
    }

    @Test
    public void testValidCurrentTime() {
        String currentTimeStr = DateUtils.time();

        Assert.assertTrue(isDateValid(currentTimeStr, TIME_FORMAT), currentTimeStr + " has invalid date format");
    }

    @Test
    public void testValidTimeDiff() {
        long startDate = getDate(START_DATE).getTime();
        String timeStr = DateUtils.timeDiff(startDate);

        Assert.assertTrue(isDateValid(timeStr, TIME_FORMAT), startDate + " has invalid date format");
    }

    @Test
    public void testValidTimeFormat() {
        long startDate = getDate(START_DATE).getTime();
        String timeStr = DateUtils.timeFormat(startDate);

        Assert.assertTrue(isDateValid(timeStr, TIME_FORMAT), startDate + " has invalid date format");
    }

    private Date getDate(String dateStr) {
        Date date = new Date();
        try {
            date = new SimpleDateFormat(DATE_FORMAT).parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    private boolean isDateValid(String dateStr, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);
        try {
            sdf.parse(dateStr);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

}
