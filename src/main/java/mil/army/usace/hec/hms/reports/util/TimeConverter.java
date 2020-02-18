package mil.army.usace.hec.hms.reports.util;

import hec.heclib.util.HecTime;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;

public class TimeConverter {

    private TimeConverter(){}

    public static ZonedDateTime toZonedDateTime(HecTime hecTime) {
        return ZonedDateTime.parse(hecTime.getXMLDateTime(0));
    }

    public static HecTime toHecTime(ZonedDateTime zonedDateTime) {
        return new HecTime(Date.from(zonedDateTime.toInstant()), 0);
    }

    public static String toString(ZonedDateTime zonedDateTime, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date toDate = Date.from(zonedDateTime.toInstant());

        return simpleDateFormat.format(toDate);
    }
}
