package com.chrisworks.personal.inventorysystem.Backend.Utility.BackgroundJobs;

import com.chrisworks.personal.inventorysystem.Backend.Entities.POJO.*;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.BusinessOwnerRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.EventLogRepository;
import com.chrisworks.personal.inventorysystem.Backend.Repositories.SummaryReportsRepository;
import com.chrisworks.personal.inventorysystem.Backend.Services.MailServices;
import com.sendgrid.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.chrisworks.personal.inventorysystem.Backend.Configurations.Interceptors.CustomInterceptor.URI_MAP;

/**
 * @author Chris_Eteka
 * @since 1/9/2020
 * @email chriseteka@gmail.com
 */
@Component
public class HourlyJobs {

    private static final long JOB_FIXED_RATE_TIME = 1000L * 60L * 60L; //One hour in millis.

    private final BusinessOwnerRepository businessOwnerRepository;

    private final EventLogRepository logRepository;

    private final SummaryReportsRepository reportsRepository;

    private final MailServices mailServices;

    @Value("${email.sender}") private String emailSender;

    public HourlyJobs(BusinessOwnerRepository businessOwnerRepository, EventLogRepository logRepository,
                      MailServices mailServices, SummaryReportsRepository reportsRepository) {
        this.businessOwnerRepository = businessOwnerRepository;
        this.logRepository = logRepository;
        this.mailServices = mailServices;
        this.reportsRepository = reportsRepository;
    }

    @Scheduled(initialDelay = JOB_FIXED_RATE_TIME, fixedRate = JOB_FIXED_RATE_TIME)
    private void AlertBusinessOwnersOfEventsInThePastHour(){

        //Try to send any yet to be delivered summary report
        List<SummaryReports> allYetToDeliverReports = reportsRepository.findAllByDeliveredIsFalse();

        allYetToDeliverReports
                .parallelStream()
                .forEach(summaryReports -> {

                    EmailObject emailObject = new EmailObject();
                    EmailAttachments attachments = new EmailAttachments();

                    byte[] pdf = summaryReports.getData();

                    emailObject.setMessageSender(emailSender);
                    emailObject.setMessageTitle("Daily Reports");
                    emailObject.setMessageBody("Download the daily report found in this email.");
                    emailObject.setMessageReceiver(summaryReports.getReportFor());
                    attachments.setFileName("Daily-Summary-for: " + summaryReports.getCreatedDate() + ".pdf");
                    String attachedFile = Base64.getEncoder().encodeToString(pdf);
                    attachments.setAttachment(attachedFile);
                    attachments.setAttachmentType("application/pdf");
                    emailObject.setAttachments(Collections.singletonList(attachments));

                    Response response = mailServices.sendEmailToAnyUser(emailObject);

                    if (response.getStatusCode() == 202){
                        summaryReports.setDelivered(true);
                        reportsRepository.save(summaryReports);
                    }
                });

        List<EventLog> eventLogList = businessOwnerRepository.findAll()
                .parallelStream()
                .map(BusinessOwner::getBusinessOwnerEmail)
                .map(logRepository::findAllByNoticeForAndAdminNotifiedFalse)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (!eventLogList.isEmpty()){

            eventLogList
                    .parallelStream()
                    .forEach(eventLog -> {

                        //Make this a html organized body.
                        EmailObject emailObject = new EmailObject();
                        emailObject.setMessageSender(emailSender);
                        emailObject.setMessageReceiver(eventLog.getNoticeFor());
                        emailObject.setMessageTitle(eventLog.getEventType().toString());
                        emailObject.setMessageBody(eventLog.getEventBody());

                        Response response = mailServices.sendEmailToAnyUser(emailObject);

                        if (response.getStatusCode() == 202){
                            eventLog.setAdminNotified(true);
                            logRepository.save(eventLog);
                        }
                    });
        }
    }

    @Scheduled(initialDelay = JOB_FIXED_RATE_TIME, fixedRate = JOB_FIXED_RATE_TIME)
    private void RemoveExpiredData(){
        //Remove data from the global map created to hold URIs that may trigger twice
        URI_MAP.entrySet().removeIf(a -> a.getValue().getExpirationTime().isBefore(LocalDateTime.now()));
    }
}
