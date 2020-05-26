package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.EOD_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Services.EndOfDayServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.stream.Stream;

@RestController
@RequestMapping("/endOfDay")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class EndOfDayController {

    private EndOfDayServices endOfDayServices;

    @Autowired
    public EndOfDayController(EndOfDayServices endOfDayServices) {
        this.endOfDayServices = endOfDayServices;
    }

    @GetMapping(path = "/compute", produces = "application/json")
    public ResponseEntity<?> getEndOfDay(@RequestParam(defaultValue = "complete") String eodFor){

        return ResponseEntity.ok(endOfDayServices.generateEndOfDayReport(getEODTypeFromRequest(eodFor), null, null));
    }

    @GetMapping(path = "/byDate", produces = "application/json")
    public ResponseEntity<?> getEndOfDayByDate(@RequestParam Date date,
                                               @RequestParam(defaultValue = "complete") String eodFor){

        return ResponseEntity.ok(endOfDayServices.generateEndOfDayReportFor(getEODTypeFromRequest(eodFor), date));
    }

    @GetMapping(path = "/byDateInterval", produces = "application/json")
    public ResponseEntity<?> getEndOfDayByDateInterval(@RequestParam Date from,
                                                       @RequestParam Date to,
                                                       @RequestParam(defaultValue = "complete") String eodFor){

        return ResponseEntity.ok(endOfDayServices.generateEndOfDayReportBetween(getEODTypeFromRequest(eodFor), from, to));
    }

    private EOD_TYPE getEODTypeFromRequest(String eodFor) {

        String eod = eodFor.toUpperCase();

        boolean match = Stream.of(EOD_TYPE.values()).anyMatch(e -> e.toString().equalsIgnoreCase(eod));
        if (!match) throw new InventoryAPIOperationException("Invalid end of day",
                "Invalid end of day type passed, review your inputs and try again", null);

        return EOD_TYPE.valueOf(eod);
    }
}
