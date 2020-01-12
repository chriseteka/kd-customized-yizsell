package com.chrisworks.personal.inventorysystem.Backend.Utility.BackgroundJobs;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.EmailAttachments;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.EmailObject;
import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.SummaryReports;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SummaryReportsRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.MailServices;
import com.chrisworks.personal.inventorysystem.Backend.Utility.GenerateReport;
import com.sendgrid.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Collections;
import java.util.Date;

import static com.chrisworks.personal.inventorysystem.Backend.Utility.Utility.formatDate;

/**
 * @author Chris_Eteka
 * @since 1/9/2020
 * @email chriseteka@gmail.com
 */
@Component
public class QuarterlyJobs {

    private static final long JOB_FIXED_RATE_TIME = 1000L * 60L * 60L * 24L * 30L * 3L; //Ninety days in millis.

    private final BusinessOwnerRepository businessOwnerRepository;

    private final SummaryReportsRepository reportsRepository;

    private final MailServices mailServices;

    private final GenerateReport report;

    private Date lastReportDate = new Date();

    private Date nextReportDate;

    @Value("${email.sender}") private String emailSender;

    public QuarterlyJobs(BusinessOwnerRepository businessOwnerRepository, MailServices mailServices,
                       SummaryReportsRepository reportsRepository, GenerateReport report) {
        this.businessOwnerRepository = businessOwnerRepository;
        this.mailServices = mailServices;
        this.reportsRepository = reportsRepository;
        this.report = report;
    }

    @Scheduled(initialDelay = JOB_FIXED_RATE_TIME, fixedRate = JOB_FIXED_RATE_TIME)
    private void sendDailySummaryToBusinessOwners(){

        final Date today = new Date();

        nextReportDate = today;

        String notice = "Quarterly Reports Summary";
        businessOwnerRepository.findAll()
                .parallelStream()
                .forEach(businessOwner -> {

                    EmailObject emailObject = new EmailObject();
                    EmailAttachments attachments = new EmailAttachments();

                    byte[] pdf = report.generate(businessOwner, lastReportDate, nextReportDate, notice);

                    emailObject.setMessageSender(emailSender);
                    emailObject.setMessageTitle("Daily Reports");
                    emailObject.setMessageBody("Download the daily report found in this email.");
                    emailObject.setMessageReceiver(businessOwner.getBusinessOwnerEmail());
                    attachments.setFileName("Daily-Summary-for: " + formatDate(today) + ".pdf");
                    String attachedFile = Base64.getEncoder().encodeToString(pdf);
                    attachments.setAttachment(attachedFile);
                    attachments.setAttachmentType("application/pdf");
                    emailObject.setAttachments(Collections.singletonList(attachments));

                    Response response = mailServices.sendEmailToAnyUser(emailObject);

                    SummaryReports summaryReports = new SummaryReports(pdf, businessOwner.getBusinessOwnerEmail());
                    if (response.getStatusCode() != 202)
                        reportsRepository.save(summaryReports);
                });
        lastReportDate = today;
    }
}

