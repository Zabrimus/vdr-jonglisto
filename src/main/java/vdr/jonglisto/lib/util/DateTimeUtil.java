package vdr.jonglisto.lib.util;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateTimeUtil {

    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static DateTimeFormatter longDateFormatter = DateTimeFormatter.ofPattern("EE dd. MMMM yyyy");
    private static DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private static DateTimeFormatter diffTimeFormatter = DateTimeFormatter.ofPattern("HHmm");
    private static DateTimeFormatter diffDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String toDate(LocalDateTime date) {
        return date.format(dateFormatter);
    }

    public static String toDate(Long unixTime) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneId.systemDefault()).toLocalDate().format(dateFormatter);
    }

    public static String toRestfulDate(LocalDateTime date) {
        return date.format(diffDateFormatter);
    }

    public static String toLongDate(LocalDateTime date) {
        return date.format(longDateFormatter);
    }

    public static String toTime(LocalDateTime date) {
        return date.format(timeFormatter);
    }

    public static String toTime(Long unixTime) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneId.systemDefault()).toLocalTime()
                .format(timeFormatter);
    }

    public static String toDuration(Long unixTime) {
        LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneId.systemDefault());
        LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneId.systemDefault());

        Duration duration = Duration.between(end, start);
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();

        return String.format("%02d:%02d", hours, minutes);
    }

    public static LocalDateTime toDateTime(Long unixTime) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTime), ZoneId.systemDefault());
    }

    public static LocalDateTime setTime(LocalDateTime source, String time) {
        LocalTime tmp = LocalTime.parse(time, timeFormatter);
        return source.withHour(tmp.getHour()) //
                .withMinute(tmp.getMinute());
    }

    public static LocalDateTime setDate(LocalDateTime source, String date) {
        LocalDate tmp = LocalDate.parse(date, dateFormatter);
        return source.withYear(tmp.getYear()) //
                .withMonth(tmp.getMonthValue()) //
                .withDayOfMonth(tmp.getDayOfMonth());
    }

    public static String toRestfulTime(LocalDateTime date) {
        return date.format(diffTimeFormatter);
    }

    public static String toDuration(LocalDateTime start, LocalDateTime stop) {
        long hours = ChronoUnit.HOURS.between(start, stop);
        long minutes = ChronoUnit.MINUTES.between(start, stop) % 60;

        return String.format("%02d:%02d", hours, minutes);
    }
    
    public static LocalDateTime toLocalDateTime(String date, int time) {
        String toParse = String.format("%sT%02d:%02d:00", date, time/100, time%100);
        return LocalDateTime.parse(toParse);
    }
}
