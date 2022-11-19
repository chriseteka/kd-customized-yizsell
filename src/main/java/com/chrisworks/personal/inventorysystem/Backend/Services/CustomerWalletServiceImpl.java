package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Customer;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.CustomerWallet;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.CustomerWallet.History;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIResourceNotFoundException;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.CustomerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.CustomerWalletRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerWalletServiceImpl implements CustomerWalletService {

  private final CustomerRepository customerRepository;
  private final CustomerWalletRepository customerWalletRepository;

  @Override
  public CustomerWallet fetchWalletByCustomerId(Long customerId) {
    return optionalWalletByCustomerId(customerId)
        .orElseThrow(() -> new InventoryAPIResourceNotFoundException("404", "Wallet not found", null));
  }

  @Override
  public boolean addMoneyToWallet(Long customerId, BigDecimal amount) {
    return execActionOnWallet(customerId, wallet -> refundChargeBackToWallet(wallet, amount));
  }

  @Override
  public boolean chargeWallet(Long customerId, BigDecimal amount) {
    return execActionOnWallet(customerId, wallet -> chargeWallet(wallet, amount));
  }

  @Override
  public boolean chargeWallet(CustomerWallet customerWallet, BigDecimal amount) {

    final boolean charged = customerWallet.chargeWallet(amount);
    customerWalletRepository.save(customerWallet);
    return charged;
  }

  @Override
  public boolean setWalletCreditLimit(Long customerId, BigDecimal amount) {
    return execActionOnWallet(
        customerId,
        wallet -> {
          wallet.changeCreditLimitTo(amount);
          final CustomerWallet updatedWallet = customerWalletRepository.save(wallet);
          return updatedWallet.getCreditLimit().compareTo(amount) == 0;
        });
  }

  @Override
  public boolean refundChargeBackToWallet(Long customerId, BigDecimal amount) {
    return addMoneyToWallet(customerId, amount);
  }

  @Override
  public boolean refundChargeBackToWallet(CustomerWallet customerWallet, BigDecimal amount) {
    final boolean added = customerWallet.addMoney(amount);
    customerWalletRepository.save(customerWallet);
    return added;
  }

  @Override
  public List<History> retrieveWalletHistory(Long customerId) {
    return fetchWalletByCustomerId(customerId).getChangeLogs();
  }

  private Optional<CustomerWallet> optionalWalletByCustomerId(Long customerId) {
    return customerRepository.findById(customerId).map(Customer::getCustomerWallet);
  }

  private boolean execActionOnWallet(Long customerId, Function<CustomerWallet, Boolean> action) {
    return optionalWalletByCustomerId(customerId)
        .map(action)
        .orElse(false);
  }
}
