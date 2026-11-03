package anzihe.com.agent.utils;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.TimeZoneUtil;
import cn.hutool.core.date.ZoneUtil;
import com.networknt.schema.format.TimeFormat;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static anzihe.com.agent.utils.DateTimeFormatterUtil.DATE_TIME_STR;

public class TimeUtils {

    public static String getCurrentTime(){
        return ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern(DateTimeFormatterUtil.DATE_TIME_STR));

    }
}
