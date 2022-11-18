package com.chrisworks.personal.inventorysystem.Backend.Services;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.CustomerWallet;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.CustomerWallet.History;
import java.math.BigDecimal;
import java.util.List;

public interface CustomerWalletService {

  CustomerWallet fetchWalletByCustomerId(Long customerId);

  boolean addMoneyToWallet(Long customerId, BigDecimal amount);
  boolean chargeWallet(Long customerId, BigDecimal amount);
  boolean chargeWallet(CustomerWallet customerWallet, BigDecimal amount);
  boolean setWalletCreditLimit(Long customerId, BigDecimal amount);
  boolean refundChargeBackToWallet(Long customerId, BigDecimal amount);
  boolean refundChargeBackToWallet(CustomerWallet customerWallet, BigDecimal amount);
  List<History> retrieveWalletHistory(Long customerId);

}
