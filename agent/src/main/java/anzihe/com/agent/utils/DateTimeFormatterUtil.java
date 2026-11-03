package anzihe.com.agent.utils;

import java.time.format.DateTimeFormatter;

public class DateTimeFormatterUtil {

    public static final String DATE_TIME_STR = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_STR = "yyyy-MM-dd";
    public static final String TIME_STR = "HH:mm:ss";
    public static final String TIMESTAMP_STR = "yyyyMMddHHmmss";
    public static final String UTC_TIMESTAMP_STR = "yyyyMMddmmHHssSSS";

    public static final  DateTimeFormatter UTC_TIMESTAMP_FORMAT =  DateTimeFormatter.ofPattern(UTC_TIMESTAMP_STR);
    public static final  DateTimeFormatter TIMESTAMP_FORMAT =  DateTimeFormatter.ofPattern(TIMESTAMP_STR);
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern(DATE_TIME_STR);
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_STR);
    public static final DateTimeFormatter TIME_FORMAT =  DateTimeFormatter.ofPattern(TIME_STR);

}