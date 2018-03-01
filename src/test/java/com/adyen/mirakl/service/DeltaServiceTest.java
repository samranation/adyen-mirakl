package com.adyen.mirakl.service;

import com.adyen.mirakl.AdyenMiraklConnectorApp;
import com.adyen.mirakl.domain.MiraklDelta;
import com.adyen.mirakl.repository.MiraklDeltaRepository;
import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = AdyenMiraklConnectorApp.class)
@Transactional
public class DeltaServiceTest {

    @Autowired
    private DeltaService deltaService;

    @Autowired
    private MiraklDeltaRepository miraklDeltaRepository;

    private Date date = new Date(0);

    @Test
    public void onlyGetsTheLatestDeltaEvenThoughMoreThanShouldNotExist(){
        final MiraklDelta miraklDelta1 = new MiraklDelta();
        miraklDelta1.setShopDelta(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).plusDays(1));

        final MiraklDelta miraklDelta2 = new MiraklDelta();
        miraklDelta2.setShopDelta(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).plusDays(2));

        final MiraklDelta miraklDelta3 = new MiraklDelta();
        miraklDelta3.setShopDelta(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));

        miraklDeltaRepository.save(ImmutableList.of(miraklDelta1, miraklDelta2, miraklDelta3));
        miraklDeltaRepository.flush();

        final Date result = deltaService.getShopDelta();
        Assertions.assertThat(result).hasSameTimeAs(date);
    }

    @Test
    public void noCurrentDeltaReturnsNull(){
        final Date result = deltaService.getShopDelta();
        Assertions.assertThat(result).isNull();
    }

    @Test
    public void createNewDeltaWithExisting(){
        final MiraklDelta miraklDelta1 = new MiraklDelta();
        miraklDelta1.setShopDelta(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()).plusDays(1));
        final Long id = miraklDeltaRepository.saveAndFlush(miraklDelta1).getId();

        deltaService.createNewShopDelta();

        final List<MiraklDelta> all = miraklDeltaRepository.findAll();
        Assertions.assertThat(all.size()).isEqualTo(1);
        final MiraklDelta miraklDelta = all.iterator().next();
        Assertions.assertThat(miraklDelta.getId()).isEqualTo(id);
        Assertions.assertThat(miraklDelta.getShopDelta()).isAfter(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

    @Test
    public void createDeltaWhenNonAlreadyExist(){
        deltaService.createNewShopDelta();

        final List<MiraklDelta> all = miraklDeltaRepository.findAll();
        Assertions.assertThat(all.size()).isEqualTo(1);
        final MiraklDelta miraklDelta = all.iterator().next();
        Assertions.assertThat(miraklDelta.getShopDelta()).isAfter(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
    }

}
