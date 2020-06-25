package com.chrisworks.personal.inventorysystem.Backend.Utility;

import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author Chris_Eteka
 * @since 12/9/2019
 * @email chriseteka@gmail.com
 */
public class Utility {

    private static final String NAIRA_SIGN = "\u20a6 ";

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

    public static BigDecimal computeWeightedPrice(List<Integer> weights, List<BigDecimal> prices){

        if (weights.size() != prices.size()) throw new InventoryAPIOperationException
                ("Error in computation", "Error in computing weighted price, review your inputs and try again", null);

        int count;
        BigDecimal weightedProductSum = BigDecimal.ZERO;
        BigDecimal weightSum = BigDecimal.valueOf(weights.stream().reduce(0, Integer::sum));

        for (count = 0; count < weights.size(); count++){
            weightedProductSum = weightedProductSum.add(prices.get(count)
                    .multiply(BigDecimal.valueOf(weights.get(count))));
        }

        return weightedProductSum.divide(weightSum, BigDecimal.ROUND_HALF_EVEN);
    }

    public static String formatMoney(Object money){
        DecimalFormat df = new DecimalFormat("#, ###.00");
        return NAIRA_SIGN + df.format(money);
    }

    public static Gson getGSon(){

        return new GsonBuilder()
            .registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY)
            .registerTypeAdapter(Date.class, new DateDeserializer()).create();
    }

    private static class DateDeserializer implements JsonDeserializer<Date> {

        private final String[] DATE_FORMATS = new String[] {"KK:mm:ss a", "MMM dd, yyyy"};

        @Override
        public Date deserialize(JsonElement jsonElement, Type typeOF,
                                JsonDeserializationContext context) throws JsonParseException {
            for (String format : DATE_FORMATS) {
                try {
                    return new SimpleDateFormat(format, Locale.US).parse(jsonElement.getAsString());
                } catch (ParseException ignored) {}
            }
            throw new JsonParseException("Unparseable date: \"" + jsonElement.getAsString()
                    + "\". Supported formats: " + Arrays.toString(DATE_FORMATS));
        }
    }
}
