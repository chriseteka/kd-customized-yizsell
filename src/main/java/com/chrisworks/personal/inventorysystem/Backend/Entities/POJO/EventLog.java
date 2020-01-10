package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import com.chrisworks.personal.inventorysystem.Backend.Entities.ENUM.APPLICATION_EVENTS;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.TemporalType.DATE;
import static javax.persistence.TemporalType.TIME;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "EventLogs")
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long EventLogId;

    @Temporal(TIME)
    private Date eventTime;

    @Temporal(DATE)
    private Date eventDate;

    private String eventOccurredIn;

    private String eventPlaceName;

    private String eventPlaceAddress;

    private String sellerName;

    private String sellerMail;

    private String sellerPhone;

    @Enumerated(EnumType.STRING)
    private APPLICATION_EVENTS eventType;

    @Lob
    @Column(length = 100000)
    private String eventBody;

    private String noticeFor;

    private Boolean adminNotified = false;
}
