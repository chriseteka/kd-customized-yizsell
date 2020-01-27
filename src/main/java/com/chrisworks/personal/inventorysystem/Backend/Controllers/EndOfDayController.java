package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Services.EndOfDayServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

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
    public ResponseEntity<?> getEndOfDay(){

        return ResponseEntity.ok(endOfDayServices.generateEndOfDayReport());
    }

    @GetMapping(path = "/byDate", produces = "application/json")
    public ResponseEntity<?> getEndOfDayByDate(@RequestParam Date date){

        return ResponseEntity.ok(endOfDayServices.generateEndOfDayReportFor(date));
    }

    @GetMapping(path = "/byDateInterval", produces = "application/json")
    public ResponseEntity<?> getEndOfDayByDateInterval(@RequestParam Date from,
                                                       @RequestParam Date to){

        return ResponseEntity.ok(endOfDayServices.generateEndOfDayReportBetween(from, to));
    }
}
