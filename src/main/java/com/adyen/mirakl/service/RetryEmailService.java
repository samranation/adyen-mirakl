package com.adyen.mirakl.service;

import com.adyen.mirakl.domain.ProcessEmail;
import com.adyen.mirakl.domain.enumeration.EmailState;
import com.adyen.mirakl.repository.ProcessEmailRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RetryEmailService {

    @Resource
    private ProcessEmailRepository processEmailRepository;
    @Resource
    private MailService mailService;

    public void retryFailedEmails(){
        final List<ProcessEmail> byState = processEmailRepository.findByState(EmailState.FAILED);
        if(CollectionUtils.isEmpty(byState)){
           return;
        }
        byState.forEach(email -> mailService.sendEmail(email.getTo(), email.getSubject(), email.getContent(), email.isMultipart(), email.isHtml()));
    }

}
