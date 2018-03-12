package com.adyen.mirakl.config;

import com.adyen.mirakl.AdyenMiraklConnectorApp;
import com.adyen.mirakl.service.MailService;
import com.google.common.io.Resources;
import com.mirakl.client.core.internal.mapper.CustomObjectMapper;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.domain.shop.MiraklShops;
import io.github.jhipster.config.JHipsterProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.spring4.SpringTemplateEngine;

import java.net.URL;
import java.util.Locale;

import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdyenMiraklConnectorApp.class)
public class MailTemplateServiceTest {


    private MailTemplateService mailTemplateService;

    @Mock
    private MailService mailServiceMock;
    @Autowired
    private JHipsterProperties jHipsterProperties;
    @Autowired
    private SpringTemplateEngine templateEngine;
    @Autowired
    private MessageSource messageSource;

    @Before
    public void setup(){
        mailTemplateService = new MailTemplateService(jHipsterProperties, mailServiceMock, templateEngine, messageSource);
    }

    @Test
    public void testSendEmailFromTemplateNoUser() throws Exception {
        final URL url = Resources.getResource("miraklRequests/miraklShopsMock.json");
        final CustomObjectMapper objectMapper = new CustomObjectMapper();
        final MiraklShop miraklShop = objectMapper.readValue(url, MiraklShops.class).getShops().iterator().next();

        mailTemplateService.sendMiraklShopEmailFromTemplate(miraklShop, Locale.ENGLISH, "testMiraklShopEmail", "email.test.title");

        verify(mailServiceMock).sendEmail("adyen-mirakl-cb966314-55c3-40e6-91f7-db6d8f0be825@mailinator.com", "test title", "<html>test title, http://127.0.0.1:8080, Mr, Ford, TestData, null/mmp/shop/account/shop/5073</html>\n", false, true);
    }

}
