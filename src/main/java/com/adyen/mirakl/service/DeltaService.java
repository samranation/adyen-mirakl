package com.adyen.mirakl.service;

import com.adyen.mirakl.domain.MiraklDelta;
import com.adyen.mirakl.repository.MiraklDeltaRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class DeltaService {

    @Resource
    private MiraklDeltaRepository miraklDeltaRepository;

    public Date getShopDelta() {
        final Optional<MiraklDelta> firstByOrderByIdDesc = miraklDeltaRepository.findFirstByOrderByIdDesc();
        return firstByOrderByIdDesc
            .map(miraklDelta -> Date.from(firstByOrderByIdDesc.get().getShopDelta().toInstant()))
            .orElse(null);
    }

    public void createNewShopDelta() {
        final Optional<MiraklDelta> firstByOrderByIdDesc = miraklDeltaRepository.findFirstByOrderByIdDesc();
        final ZonedDateTime now = ZonedDateTime.now();

        if(firstByOrderByIdDesc.isPresent()){
            final MiraklDelta entity = firstByOrderByIdDesc.get();
            entity.setShopDelta(now);
            miraklDeltaRepository.saveAndFlush(entity);
        }else{
            final MiraklDelta miraklDelta = new MiraklDelta();
            miraklDelta.setShopDelta(now);
            miraklDeltaRepository.saveAndFlush(miraklDelta);
        }

    }
}
