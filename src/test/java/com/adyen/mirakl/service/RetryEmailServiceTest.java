package com.adyen.mirakl.service;

import com.adyen.mirakl.AdyenMiraklConnectorApp;
import com.adyen.mirakl.domain.EmailError;
import com.adyen.mirakl.domain.ProcessEmail;
import com.adyen.mirakl.domain.enumeration.EmailState;
import com.adyen.mirakl.repository.EmailErrorsRepository;
import com.adyen.mirakl.repository.ProcessEmailRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Autowired
    private EmailErrorsRepository emailErrorsRepository;

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

    @Test
    public void shouldRemoveSentEmailsAndErrors(){
        final ProcessEmail processEmail = createProcessEmail("to1", "subject", "content", false, false, EmailState.SENT);

        final EmailError emailError1 = new EmailError();
        final EmailError emailError2 = new EmailError();
        final EmailError emailError3 = new EmailError();
        emailError1.setError("error1");
        emailError2.setError("error2");
        emailError3.setError("error3");
        processEmail.addEmailErrors(emailError1);
        processEmail.addEmailErrors(emailError2);
        processEmail.addEmailErrors(emailError3);
        emailErrorsRepository.saveAndFlush(emailError1);
        emailErrorsRepository.saveAndFlush(emailError2);
        emailErrorsRepository.saveAndFlush(emailError3);

        processEmailRepository.saveAndFlush(processEmail);

        final ProcessEmail processEmail2 = createProcessEmail("to2", "subject", "content", false, false, EmailState.FAILED);
        final EmailError emailError4 = new EmailError();
        emailError4.setError("error4");

        processEmail2.addEmailErrors(emailError4);
        emailErrorsRepository.saveAndFlush(emailError4);
        processEmailRepository.saveAndFlush(processEmail2);

        createProcessEmail("to3", "subject", "content", false, false, EmailState.PROCESSING);
        createProcessEmail("to4", "subject", "content", false, false, EmailState.SENT);

        retryEmailService.removeSentEmails();

        final List<ProcessEmail> all = processEmailRepository.findAll();
        Assertions.assertThat(all.size()).isEqualTo(2);

        final ProcessEmail remaining1 = processEmailRepository.findExisting("to2", "subject", "content", false, false).orElse(null);
        final ProcessEmail remaining2 = processEmailRepository.findExisting("to3", "subject", "content", false, false).orElse(null);
        Assertions.assertThat(remaining1).isNotNull();
        Assertions.assertThat(remaining1.getState()).isEqualTo(EmailState.FAILED);
        Assertions.assertThat(remaining1.getEmailErrors().size()).isOne();

        Assertions.assertThat(remaining2).isNotNull();
        Assertions.assertThat(remaining2.getState()).isEqualTo(EmailState.PROCESSING);

        final List<EmailError> allErrors = emailErrorsRepository.findAll();
        Assertions.assertThat(allErrors.size()).isOne();
        Assertions.assertThat(allErrors.iterator().next().getError()).isEqualTo("error4");
    }


    public ProcessEmail createProcessEmail(String to, String subject, String content, boolean isMultipart, boolean isHtml, EmailState emailState){
        final ProcessEmail processEmail = new ProcessEmail();
        processEmail.setTo(to);
        processEmail.setSubject(subject);
        processEmail.setContent(content);
        processEmail.isMultipart(isMultipart);
        processEmail.isHtml(isHtml);
        processEmail.setState(emailState);
        return processEmailRepository.saveAndFlush(processEmail);
    }

}
