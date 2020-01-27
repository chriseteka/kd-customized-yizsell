package com.chrisworks.personal.inventorysystem.Backend.Services;

import java.util.Date;

public interface EndOfDayServices {

    EndOfDayServicesImpl.EndOfDayReport generateEndOfDayReport();

    EndOfDayServicesImpl.EndOfDayReport generateEndOfDayReportFor(Date anyDate);

    EndOfDayServicesImpl.EndOfDayReport generateEndOfDayReportBetween(Date from, Date to);
}
