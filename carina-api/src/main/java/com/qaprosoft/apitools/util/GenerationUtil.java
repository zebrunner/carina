package com.qaprosoft.apitools.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

public class GenerationUtil {
	public static final String DEFAULT_TIME_ZONE = "America/Los_Angeles";

	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

	private static final Random RANDOM = new Random();

	public static String generateTime(String format, int offset, int calendarUnit, String timeZone) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		Calendar calendar = Calendar.getInstance();
		if (timeZone != null) {
			sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		}
		calendar.add(calendarUnit, offset);
		return sdf.format(calendar.getTime());
	}

	public static String generateTime(String format, int offset, int calendarUnit) {
		return generateTime(format, offset, calendarUnit, DEFAULT_TIME_ZONE);
	}

	private static String generateBase(int keySize) {
		String base = "";
		for (int i = 0; i < keySize; i++) {
			base += String.valueOf(RANDOM.nextInt(9));
		}
		return base;
	}

	public static String generateWord(int keySize) {
		StringBuilder result = new StringBuilder();
		String base = generateBase(keySize);
		int position = RANDOM.nextInt(ALPHABET.length() - 1);
		int sign = -1;
		for (int i = 0; i < keySize; i++) {

			int step = Integer.valueOf(base.substring(i, i + 1)) * sign;
			if (position + step > 0 && position + step < ALPHABET.length() - 1) {
				position += step;
			} else {
				position -= step;
			}
			result.append(ALPHABET.charAt(position));
			sign *= -1;
		}
		return result.toString();
	}
	
	public static String generateNumber(int keySize) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < keySize; i++) {
			result.append(RANDOM.nextInt(10));
		}
		return result.toString();
	}

	// public static Calendar setTimeZone(Calendar inCalendar, String tz) {
	// long inTime = inCalendar.getTime().getTime();
	// long outTime = inTime + TimeZone.getTimeZone(tz).getRawOffset();
	// Calendar outCalendar = Calendar.getInstance(TimeZone.getTimeZone(tz));
	// outCalendar.setTimeInMillis(outTime);
	// return outCalendar;
	// }
}
