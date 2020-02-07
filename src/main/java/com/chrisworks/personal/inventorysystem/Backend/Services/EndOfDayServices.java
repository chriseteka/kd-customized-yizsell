package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.EOD_TYPE;

import java.util.Date;

public interface EndOfDayServices {

    EndOfDayServicesImpl.EndOfDayReport generateEndOfDayReport(EOD_TYPE eod_type);

    EndOfDayServicesImpl.EndOfDayReport generateEndOfDayReportFor(EOD_TYPE eod_type, Date anyDate);

    EndOfDayServicesImpl.EndOfDayReport generateEndOfDayReportBetween(EOD_TYPE eod_type, Date from, Date to);
}
