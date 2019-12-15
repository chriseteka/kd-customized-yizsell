package com.chrisworks.personal.inventorysystem.Backend.Utility.Events;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

@Data
public class OnRegistrationCompleteEvent extends ApplicationEvent {

    private BusinessOwner businessOwner;

    public OnRegistrationCompleteEvent(BusinessOwner businessOwner) {

        super(businessOwner);
        this.businessOwner = businessOwner;
    }
}
