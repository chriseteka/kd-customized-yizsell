package com.chrisworks.personal.inventorysystem.Backend.Services;

import java.util.Date;

public interface EndOfDayServices {

    void generateEndOfDayReport();

    void generateEndOfDayReportFor(Date anyDate);

    void generateEndOfDayReportBetween(Date from, Date to);
}
