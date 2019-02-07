package uk.gov.justice.digital.hmpps.riskprofiler.utils;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;


public class FileFormatUtils {

    public static DateTimeFormatter DATETIMEFORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public static String createTimestampFile(String filename, Date timestamp) {
        var lastExt = filename.lastIndexOf(".");
        var now = timestamp.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        return String.format("%s-%s.%s",
                StringUtils.left(filename, lastExt), DATETIMEFORMAT.format(now),
                StringUtils.substring(filename, lastExt+1));
    }

    public static LocalDateTime extractTimestamp(String filename) {
        try {
            var lastExt = filename.lastIndexOf(".");
            return LocalDateTime.parse(StringUtils.substring(filename, lastExt - 17, lastExt), DATETIMEFORMAT);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

}
