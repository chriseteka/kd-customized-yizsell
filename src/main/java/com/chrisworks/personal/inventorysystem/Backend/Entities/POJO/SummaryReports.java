package com.chrisworks.personal.inventorysystem.Backend.Entities.POJO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * @author Chris_Eteka
 * @since 1/11/2020
 * @email chriseteka@gmail.com
 */
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "SummaryReports")
public class SummaryReports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long SummaryReportId;

    @Temporal(TemporalType.DATE)
    private Date createdDate = new Date();

    @Lob
    private byte[] data;

    private String reportFor;

    private boolean delivered = false;

    public SummaryReports(byte[] data, String reportFor) {
        this.data = data;
        this.reportFor = reportFor;
    }
}
