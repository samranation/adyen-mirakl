package com.adyen.mirakl.scheduling;


import javax.annotation.Resource;
import com.adyen.mirakl.service.RetryEmailService;
import com.adyen.mirakl.service.ShopService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.adyen.mirakl.service.DocService;
import com.adyen.mirakl.service.ShopService;

@Service
@Profile({"dev", "prod"})
public class SchedulerTrigger {

    @Resource
    private ShopService shopService;

    @Resource
    private DocService docService;

    @Resource
    private RetryEmailService retryEmailService;

    @Scheduled(cron = "${application.shopUpdaterCron}")
    public void runShopUpdates() {
        shopService.retrieveUpdatedShops();
    }

    @Scheduled(cron = "${application.docsUpdaterCron}")
    public void runDocsUpdates() {
        docService.retrieveBankproofAndUpload();
    }

    @Scheduled(cron = "${application.emailRetryCron}")
    public void retryEmails(){
        retryEmailService.retryFailedEmails();
    }

    @Scheduled(cron = "${application.removeSentEmailsCron}")
    public void removeSentEmails(){
        retryEmailService.removeSentEmails();
    }

    public void setShopService(final ShopService shopService) {
        this.shopService = shopService;
    }

    public void setDocService(DocService docService) {
        this.docService = docService;
    }

}
