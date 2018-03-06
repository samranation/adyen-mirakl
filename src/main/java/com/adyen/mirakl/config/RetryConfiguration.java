package com.adyen.mirakl.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.listener.RetryListenerSupport;

@Configuration
@EnableRetry
@EnableAspectJAutoProxy
@ImportResource("classpath:/retryadvice.xml")
public class RetryConfiguration {

    public class MailSupportListener extends RetryListenerSupport {

        private final Logger log = LoggerFactory.getLogger(MailSupportListener.class);

        @Override
        public <T, E extends Throwable> void onError(final RetryContext context, final RetryCallback<T, E> callback, final Throwable throwable) {
            log.warn("Failed sending email. Times: {}", context.getRetryCount());
            super.onError(context, callback, throwable);
        }

        @Override
        public <T, E extends Throwable> void close(final RetryContext context, final RetryCallback<T, E> callback, final Throwable throwable) {
            log.error("Email was not sent, we can clean up here - e.g. save to DB");
            super.close(context, callback, throwable);
        }

    }
}
