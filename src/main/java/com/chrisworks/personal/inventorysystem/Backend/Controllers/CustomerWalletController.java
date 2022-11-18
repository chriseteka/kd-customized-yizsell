package com.chrisworks.personal.inventorysystem.Backend.Controllers;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.CustomerWallet;
import com.chrisworks.personal.inventorysystem.Backend.Services.CustomerWalletService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/{customerId}/wallet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CustomerWalletController {

  private final CustomerWalletService customerWalletService;

  @GetMapping
  public ResponseEntity<CustomerWallet> getWalletForCustomer(@PathVariable Long customerId) {

    return ResponseEntity.ok(customerWalletService.fetchWalletByCustomerId(customerId));
  }

  @GetMapping("/changelogs")
  public ResponseEntity<List<CustomerWallet.History>> getWalletChangeLogsForCustomer(
      @PathVariable Long customerId) {

    return ResponseEntity.ok(customerWalletService.retrieveWalletHistory(customerId));
  }

  @PutMapping("/{amount}")
  public ResponseEntity<Boolean> addMoneyToWallet(@PathVariable Long customerId,
      @PathVariable BigDecimal amount) {

    return ResponseEntity.ok(customerWalletService.addMoneyToWallet(customerId, amount));
  }

  @PutMapping("/credit_limit/{amount}")
  public ResponseEntity<Boolean> setCreditLimit(@PathVariable Long customerId,
      @PathVariable BigDecimal amount) {

    return ResponseEntity.ok(customerWalletService.setWalletCreditLimit(customerId, amount));
  }

}
