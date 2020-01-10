package com.chrisworks.personal.inventorysystem.Backend.Utility.Events.Listeners;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.ACCOUNT_TYPE;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.EventLog;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.Seller;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.EventLogRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SellerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Utility.Events.SellerTriggeredEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class SellerTriggeredEventListener implements ApplicationListener<SellerTriggeredEvent> {

    private final SellerRepository sellerRepository;

    private final EventLogRepository logRepository;

    @Autowired
    public SellerTriggeredEventListener(EventLogRepository logRepository, SellerRepository sellerRepository) {
        this.logRepository = logRepository;
        this.sellerRepository = sellerRepository;
    }

    @Override
    public void onApplicationEvent(SellerTriggeredEvent sellerTriggeredEvent) {

        Seller seller = sellerRepository.findDistinctBySellerEmail(sellerTriggeredEvent.getSellerMail());

        EventLog eventLog = new EventLog();

        eventLog.setNoticeFor(seller.getCreatedBy());
        eventLog.setSellerMail(seller.getSellerEmail());
        eventLog.setSellerName(seller.getSellerFullName());
        eventLog.setSellerPhone(seller.getSellerPhoneNumber());
        eventLog.setEventType(sellerTriggeredEvent.getEventType());
        eventLog.setEventDate(sellerTriggeredEvent.getEventDate());
        eventLog.setEventTime(sellerTriggeredEvent.getEventTime());
        eventLog.setEventBody(sellerTriggeredEvent.getEventDescription());

        if (seller.getAccountType().equals(ACCOUNT_TYPE.SHOP_SELLER)){

            eventLog.setEventOccurredIn("SHOP");
            eventLog.setEventPlaceName(seller.getShop().getShopName());
            eventLog.setEventPlaceAddress(seller.getShop().getShopAddress());
        }
        if (seller.getAccountType().equals(ACCOUNT_TYPE.WAREHOUSE_ATTENDANT)){

            eventLog.setEventOccurredIn("WAREHOUSE");
            eventLog.setEventPlaceName(seller.getWarehouse().getWarehouseName());
            eventLog.setEventPlaceAddress(seller.getWarehouse().getWarehouseAddress());
        }

        logRepository.save(eventLog);
    }
}
