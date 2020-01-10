package com.chrisworks.personal.inventorysystem.Backend.Utility.Events;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.APPLICATION_EVENTS;
import lombok.Data;
import org.springframework.context.ApplicationEvent;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import java.util.Date;

import static javax.persistence.TemporalType.DATE;
import static javax.persistence.TemporalType.TIME;

@Data
public class SellerTriggeredEvent extends ApplicationEvent {

    private String sellerMail;

    private String eventDescription;

    @Enumerated(EnumType.STRING)
    private APPLICATION_EVENTS eventType;

    @Temporal(DATE)
    private Date eventDate = new Date();

    @Temporal(TIME)
    private Date eventTime = new Date();

    public SellerTriggeredEvent(String sellerMail, String eventDescription, APPLICATION_EVENTS eventType) {

        super(sellerMail);
        this.sellerMail = sellerMail;
        if (eventType != null) this.eventDescription = eventType + " :: " + eventDescription;
        else this.eventDescription = eventDescription;
        this.eventType = eventType;
    }
}
