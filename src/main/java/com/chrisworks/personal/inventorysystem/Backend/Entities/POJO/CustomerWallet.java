package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.getGSon;
import static ir.cafebabe.math.utils.BigDecimalUtils.is;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.CustomerWallet.History.WalletAction;
import com.chrisworks.personal.inventorysystem.Backend.ExceptionManagement.InventoryAPIExceptions.InventoryAPIOperationException;
import com.chrisworks.personal.inventorysystem.Backend.Utility.AuthenticatedUserDetails;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "customer_wallets")
public class CustomerWallet {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Temporal(TemporalType.DATE)
  @Column(name = "createdDate")
  private Date createdDate = new Date();

  @Temporal(TemporalType.TIME)
  @Column(name = "createdTime")
  private Date createdTime = new Date();

  @Temporal(TemporalType.DATE)
  @Column(name = "updateDate")
  private Date updateDate = new Date();

  @Column(name = "lastUpdatedBy")
  private String lastUpdatedBy;

  @Lob
  @Column(name = "walletChangeLog")
  private String walletChangeLog;

  @Column(name = "balance")
  private BigDecimal balance = BigDecimal.ZERO;

  @Column(name = "creditLimit")
  private BigDecimal creditLimit = BigDecimal.ZERO;

  @Column(name = "accumulatedCredit")
  private BigDecimal accumulatedCredit = BigDecimal.ZERO;

  @Transient
  private History history = new History();

  public synchronized boolean addMoney(final BigDecimal amount) {

    if (is(amount).lte(0))
      throw new InventoryAPIOperationException("404", "Adding zero or negative amount is not allowed", null);

    buildOldState(WalletAction.ADD);

    //Try to clear up the credit if there is any
    if (is(accumulatedCredit).gt(0)) {
      if (is(amount).gt(accumulatedCredit)) {
        this.accumulatedCredit = BigDecimal.ZERO;
        this.balance = balance.add(amount.subtract(accumulatedCredit));
      }
      else {
        this.accumulatedCredit = accumulatedCredit.subtract(amount);
      }
    }
    else {
      this.balance = balance.add(amount);
    }

    return is(balance).isNonNegative() && is(accumulatedCredit).isNonNegative();
  }

  public synchronized boolean chargeWallet(final BigDecimal amount) {

    if (is(balance.add(creditLimit.subtract(accumulatedCredit))).lt(amount))
      throw new InventoryAPIOperationException
          ("404", "Cannot charge customer because they are low on balance, and their credit limit is exceeded", null);

    buildOldState(WalletAction.REMOVE);

    if (is(balance).gte(amount)) {
      this.balance = balance.subtract(amount);
    }
    else {
      this.accumulatedCredit = accumulatedCredit.add(amount);
    }

    return is(balance).isNonNegative() && is(accumulatedCredit).isNonNegative();
  }

  private void buildOldState(History.WalletAction action) {
    this.getHistory().setDate(new Date());
    this.getHistory().setWalletAction(action);
    this.getHistory().setOldAmount(this.getBalance());
    this.getHistory().setActionPerformedBy(AuthenticatedUserDetails.getUserFullName());
  }

  //Add lifecycle to monitor every action
  @PreUpdate
  public void beforeUpdate() {
    this.setUpdateDate(new Date());
    this.getHistory().setCurrentAmount(this.getBalance());
    this.setLastUpdatedBy(AuthenticatedUserDetails.getUserFullName());
    addHistoryToWalletChanges();
  }

  private static final Type historyListType = TypeToken.getParameterized(List.class, History.class).getType();
  private void addHistoryToWalletChanges() {
    final List<History> changeLogs = getChangeLogs();
    changeLogs.add(this.getHistory());
    this.setWalletChangeLog(getGSon().toJson(changeLogs, historyListType));
  }

  public List<History> getChangeLogs() {

    List<History> changeLogs = new ArrayList<>();
    if (getWalletChangeLog() != null) {
      changeLogs = getGSon().fromJson(getWalletChangeLog(), historyListType);
    }

    return changeLogs;
  }

  @Setter
  @Getter
  public static class History {
    enum WalletAction {ADD, REMOVE}

    private Date date;
    private BigDecimal oldAmount;
    private BigDecimal currentAmount;
    private WalletAction walletAction;
    private String actionPerformedBy;
  }

}
