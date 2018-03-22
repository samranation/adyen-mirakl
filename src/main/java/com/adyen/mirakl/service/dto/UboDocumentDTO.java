package com.adyen.mirakl.service.dto;

import com.adyen.model.marketpay.DocumentDetail;
import com.mirakl.client.mmp.domain.shop.document.MiraklShopDocument;

public class UboDocumentDTO {

    private String shareholderCode;
    private MiraklShopDocument miraklShopDocument;
    private DocumentDetail.DocumentTypeEnum documentTypeEnum;

    public String getShareholderCode() {
        return shareholderCode;
    }

    public void setShareholderCode(final String shareholderCode) {
        this.shareholderCode = shareholderCode;
    }

    public MiraklShopDocument getMiraklShopDocument() {
        return miraklShopDocument;
    }

    public void setMiraklShopDocument(final MiraklShopDocument miraklShopDocument) {
        this.miraklShopDocument = miraklShopDocument;
    }

    public DocumentDetail.DocumentTypeEnum getDocumentTypeEnum() {
        return documentTypeEnum;
    }

    public void setDocumentTypeEnum(final DocumentDetail.DocumentTypeEnum documentTypeEnum) {
        this.documentTypeEnum = documentTypeEnum;
    }
}
