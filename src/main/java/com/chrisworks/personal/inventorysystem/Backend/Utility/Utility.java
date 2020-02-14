package com.chrisworks.personal.inventorysystem.Backend.Utility;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 12/9/2019
 * @email chriseteka@gmail.com
 */
public class Utility {

    //Method for returning a singleton from a stream
    public static <T> Collector<T, ?, T> toSingleton() {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() > 1) {
                        throw new IllegalStateException("Could not retrieve a singleton, due to multiple elements retrieved");
                    }
                    if (list.size() == 0){
                        return null;
//                        throw new IllegalStateException("Could not retrieve a singleton due to zero element in the list");
                    }
                    return list.get(0);
                }
        );
    }

    public static Date futureDate(int noOfDaysFromToday){

        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, noOfDaysFromToday);

        return calendar.getTime();
    }

    public static Boolean isDateEqual(Date date1, Date date2){

        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");

        return fmt.format(date1).equals(fmt.format(date2));
    }

    public static long getDateDifferenceInDays(Date date1, Date date2) {

        long diff = date2.getTime() - date1.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    public static String formatDate(Date date){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd");

        Instant instant = date.toInstant();

        LocalDate localDate = instant
                .atZone(ZoneId.of("Africa/Lagos"))
                .toLocalDate();

        return localDate.format(formatter);
    }

    public static String stripStringOfExpiryDate(String s){

        if (s.contains(" Exp: ")) {

            int indexOfExpiryDate = s.indexOf(" Exp: ") + 1;
            return s.substring(0, indexOfExpiryDate);
        }
        return s;
    }

    public static BigDecimal computeWeightedPrice(int w1, BigDecimal p1, int w2, BigDecimal p2){

        return BigDecimal.ONE;
    }
}
