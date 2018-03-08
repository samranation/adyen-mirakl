package com.adyen.mirakl.service;

import com.adyen.mirakl.AdyenMiraklConnectorApp;
import com.adyen.mirakl.domain.ProcessEmail;
import com.adyen.mirakl.domain.enumeration.EmailState;
import com.adyen.mirakl.repository.ProcessEmailRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdyenMiraklConnectorApp.class)
@Transactional
public class RetryEmailServiceTest {

    @Autowired
    private RetryEmailService retryEmailService;

    @Autowired
    private ProcessEmailRepository processEmailRepository;

    @MockBean
    private MailService mailService;

    @Test
    public void shouldRetryWithOnlyFailures(){
        createProcessEmail("to1", "subject", "content", false, false, EmailState.PROCESSING);
        createProcessEmail("to2", "subject", "content", false, false, EmailState.FAILED);
        createProcessEmail("to3", "subject", "content", false, false, EmailState.SENT);
        createProcessEmail("to4", "subject", "content", false, false, EmailState.FAILED);

        retryEmailService.retryFailedEmails();

        verify(mailService, never()).sendEmail("to1", "subject", "content", false, false);
        verify(mailService).sendEmail("to2", "subject", "content", false, false);
        verify(mailService, never()).sendEmail("to3", "subject", "content", false, false);
        verify(mailService).sendEmail("to4", "subject", "content", false, false);
        verify(mailService, never()).sendEmail("to5", "subject", "content", false, false);
    }

    @Test
    public void shouldNotRetry(){
        retryEmailService.retryFailedEmails();

        verify(mailService, never()).sendEmail(anyString(), anyString(), anyString(), anyBoolean(), anyBoolean());
    }

    public void createProcessEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml, EmailState emailState){
        final ProcessEmail processEmail = new ProcessEmail();
        processEmail.setTo(to);
        processEmail.setSubject(subject);
        processEmail.setContent(content);
        processEmail.isMultipart(isMultipart);
        processEmail.isHtml(isHtml);
        processEmail.setState(emailState);
        processEmailRepository.saveAndFlush(processEmail);
    }

}
