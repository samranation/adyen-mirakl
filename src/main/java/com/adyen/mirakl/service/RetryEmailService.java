package com.adyen.mirakl.service;

import com.adyen.mirakl.domain.ProcessEmail;
import com.adyen.mirakl.domain.enumeration.EmailState;
import com.adyen.mirakl.repository.EmailErrorsRepository;
import com.adyen.mirakl.repository.ProcessEmailRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RetryEmailService {

    @Resource
    private ProcessEmailRepository processEmailRepository;
    @Resource
    private EmailErrorsRepository emailErrorsRepository;
    @Resource
    private MailService mailService;
    @Value("${miraklOperator.miraklOperatorEmail}")
    private String bcc;

    public void retryFailedEmails(){
        final List<ProcessEmail> failedEmails = processEmailRepository.findByState(EmailState.FAILED);
        if(CollectionUtils.isEmpty(failedEmails)){
           return;
        }
        failedEmails.forEach(email -> mailService.sendEmail(email.getTo(), bcc, email.getSubject(), email.getContent(), email.isMultipart(), email.isHtml()));
    }

    public void removeSentEmails(){
        final List<ProcessEmail> sentEmails = processEmailRepository.findByState(EmailState.SENT);
        if(CollectionUtils.isEmpty(sentEmails)){
            return;
        }
        sentEmails.forEach(email ->{
            emailErrorsRepository.delete(email.getEmailErrors());
            processEmailRepository.delete(email);
        });
    }

}
