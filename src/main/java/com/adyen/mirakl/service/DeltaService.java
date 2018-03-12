package com.adyen.mirakl.service;

import com.adyen.mirakl.domain.MiraklDelta;
import com.adyen.mirakl.domain.MiraklDocumentDelta;
import com.adyen.mirakl.repository.MiraklDeltaRepository;
import com.adyen.mirakl.repository.MiraklDocumentDeltaRepository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@Service
public class DeltaService {

    @Resource
    private MiraklDeltaRepository miraklDeltaRepository;

    @Resource
    private MiraklDocumentDeltaRepository miraklDocumentDeltaRepository;

    public Date getShopDelta() {
        final Optional<MiraklDelta> firstByOrderByIdDesc = miraklDeltaRepository.findFirstByOrderByIdDesc();
        return firstByOrderByIdDesc
            .map(miraklDelta -> Date.from(firstByOrderByIdDesc.get().getShopDelta().toInstant()))
            .orElse(null);
    }

    public void createNewShopDelta(ZonedDateTime delta) {
        final Optional<MiraklDelta> firstByOrderByIdDesc = miraklDeltaRepository.findFirstByOrderByIdDesc();

        if(firstByOrderByIdDesc.isPresent()){
            final MiraklDelta entity = firstByOrderByIdDesc.get();
            entity.setShopDelta(delta);
            miraklDeltaRepository.saveAndFlush(entity);
        }else{
            final MiraklDelta miraklDelta = new MiraklDelta();
            miraklDelta.setShopDelta(delta);
            miraklDeltaRepository.saveAndFlush(miraklDelta);
        }

    }

    public Date getDocumentDelta() {
        final Optional<MiraklDocumentDelta> firstByOrderByIdDesc = miraklDocumentDeltaRepository.findFirstByOrderByIdDesc();
        return firstByOrderByIdDesc.map(miraklDelta -> Date.from(firstByOrderByIdDesc.get().getDocumentDelta().toInstant())).orElse(null);
    }

    public void createNewDocumentDelta(ZonedDateTime delta) {
        final Optional<MiraklDocumentDelta> firstByOrderByIdDesc = miraklDocumentDeltaRepository.findFirstByOrderByIdDesc();

        if (firstByOrderByIdDesc.isPresent()) {
            final MiraklDocumentDelta entity = firstByOrderByIdDesc.get();
            entity.setDocumentDelta(delta);
            miraklDocumentDeltaRepository.saveAndFlush(entity);
        } else {
            final MiraklDocumentDelta miraklDelta = new MiraklDocumentDelta();
            miraklDelta.setDocumentDelta(delta);
            miraklDocumentDeltaRepository.saveAndFlush(miraklDelta);
        }

    }
}
