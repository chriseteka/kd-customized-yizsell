package com.chrisworks.personal.inventorysystem.Backend.Utility;

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
                        throw new IllegalStateException("Could not retrieve a singleton due to zero element in the list");
                    }
                    return list.get(0);
                }
        );
    }
}
