package com.chrisworks.personal.inventorysystem.Backend.Utility.Events;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.BusinessOwner;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

@EqualsAndHashCode(callSuper = false)
@Data
public class OnRegistrationCompleteEvent extends ApplicationEvent {

    private BusinessOwner businessOwner;

    public OnRegistrationCompleteEvent(BusinessOwner businessOwner) {

        super(businessOwner);
        this.businessOwner = businessOwner;
    }
}
