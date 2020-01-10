package com.chrisworks.personal.inventorysystem.Backend.Utility.BackgroundJobs;

import org.springframework.stereotype.Component;

/**
 * @author Chris_Eteka
 * @since 1/9/2020
 * @email chriseteka@gmail.com
 */
@Component
public class YearlyJobs {

    //Three hundred and sixty days in millis.
    private static final long JOB_FIXED_RATE_TIME = 1000L * 60L * 60L * 24L * 30L * 12L;
}
