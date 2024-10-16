package org.camunda.bpm.examples.custom.mappings.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtils {

  public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();

  public static Date dateInFuture(long amountToAdd, ChronoUnit amount) {
    return dateInFutureFrom(now(), amountToAdd, amount);
  }

  private static Date dateInFutureFrom(LocalDateTime from, long amountToAdd, ChronoUnit amount) {
    return toDate(from.plus(amountToAdd, amount));
  }

  // -- Converters Date/LocalDate/LocalDateTime

  public static Date toDate(LocalDateTime dateToConvert) {
    return Date.from(dateToConvert.atZone(DEFAULT_ZONE_ID).toInstant());
  }

  private static LocalDateTime now() {
    return LocalDateTime.now(DEFAULT_ZONE_ID);
  }

}
