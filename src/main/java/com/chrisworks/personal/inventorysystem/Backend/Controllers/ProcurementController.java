package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Procurement;
import com.chrisworks.personal.inventorysystem.Backend.Services.ProcurementServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Entities.ListWrapper.prepareResponse;

/**
 * @author Chris_Eteka
 * @since 6/1/2020
 * @email chriseteka@gmail.com
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/procurement")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class ProcurementController {

    private final ProcurementServices procurementServices;

    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createProcurement(@RequestBody @Valid Procurement procurement){

        return new ResponseEntity<>(procurementServices.createEntity(procurement), HttpStatus.CREATED);
    }

    @PutMapping(path = "/update", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateProcurement(@RequestParam Long procurementId,
                                               @RequestBody @Valid Procurement procurement){

        return ResponseEntity.ok(procurementServices.updateEntity(procurementId, procurement));
    }

    @GetMapping(path = "/all", produces = "application/json")
    public ResponseEntity<?> fetchAllProcurements(@RequestParam int page, @RequestParam int size){

        List<Procurement> procurements = procurementServices.getEntityList()
                .stream()
                .sorted(Comparator.comparing(Procurement::getCreatedDate)
                    .thenComparing(Procurement::getCreatedTime).reversed())
                .collect(Collectors.toList());

        return ResponseEntity.ok(prepareResponse(procurements, page, size));
    }

    @GetMapping(path = "/find", produces = "application/json")
    public ResponseEntity<?> fetchSingleProcurement(@RequestParam Long procurementId){

        return ResponseEntity.ok(procurementServices.getSingleEntity(procurementId));
    }

    @GetMapping(path = "/find/byWaybillId", produces = "application/json")
    public ResponseEntity<?> fetchProcurementByWaybillId(@RequestParam String waybillId){

        return ResponseEntity.ok(procurementServices.fetchProcurementByWaybillId(waybillId));
    }

    @DeleteMapping(path = "/delete", produces = "application/json")
    public ResponseEntity<?> deleteProcurement(@RequestParam Long procurementId){

        return ResponseEntity.ok(procurementServices.deleteEntity(procurementId));
    }
}
