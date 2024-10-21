package org.camunda.bpm.examples.custom.dmn.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtils {

  public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

  public static Date dateInFutureFrom(Date from, long amountToAdd, ChronoUnit amount) {
    return dateInFutureFrom(toLocalDateTime(from), amountToAdd, amount);
  }

  private static Date dateInFutureFrom(LocalDateTime from, long amountToAdd, ChronoUnit amount) {
    return toDate(from.plus(amountToAdd, amount));
  }

  public static Date dateInPastFrom(Date from, Duration duration) {
    return dateInPastFrom(toLocalDateTime(from), duration.toMillis(), ChronoUnit.MILLIS);
  }

  public static Date dateInPastFrom(Date from, long amountToSubtract, ChronoUnit amount) {
    return dateInPastFrom(toLocalDateTime(from), amountToSubtract, amount);
  }

  private static Date dateInPastFrom(LocalDateTime from, long amountToSubtract, ChronoUnit amount) {
    return toDate(from.minus(amountToSubtract, amount));
  }

  // -- Converters Date/LocalDate/LocalDateTime

  public static LocalDateTime toLocalDateTime(Date dateToConvert) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(dateToConvert.getTime()), DEFAULT_ZONE_ID);
  }

  public static Date toDate(LocalDateTime dateToConvert) {
    return Date.from(dateToConvert.atZone(DEFAULT_ZONE_ID).toInstant());
  }

  // -- parse

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy HH:mm");

  public static Date parseDate(String dateAsString) {
    try {
      return DATE_FORMAT.parse(dateAsString);
    } catch (ParseException ex) {
      throw new RuntimeException(ex);
    }
  }

}
