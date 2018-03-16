package com.adyen.mirakl.service;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import com.adyen.mirakl.config.Constants;
import com.adyen.model.marketpay.AccountHolderDetails;
import com.adyen.model.marketpay.BankAccountDetail;
import com.adyen.model.marketpay.DocumentDetail;
import com.adyen.model.marketpay.GetAccountHolderResponse;
import com.adyen.model.marketpay.UploadDocumentRequest;
import com.adyen.service.Account;
import com.google.common.io.Resources;
import com.mirakl.client.mmp.domain.common.FileWrapper;
import com.mirakl.client.mmp.domain.shop.document.MiraklShopDocument;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import static com.google.common.io.Files.toByteArray;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocServiceTest {
    @InjectMocks
    private DocService docService;

    @Mock
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClientMock;
    @Mock
    private Account adyenAccountServiceMock;
    @Mock
    private DeltaService deltaService;

    @Captor
    private ArgumentCaptor<UploadDocumentRequest> uploadDocumentRequestCaptor;

    @Test
    public void testRetrieveBankproofAndUpload() throws Exception {

        FileWrapper fileWrapper = mock(FileWrapper.class);
        URL url = Resources.getResource("fileuploads/BankStatement.jpg");
        File file = new File(url.getPath());

        List<MiraklShopDocument> miraklShopDocumentList = new ArrayList<>();
        MiraklShopDocument fakeDocument = new MiraklShopDocument();
        fakeDocument.setFileName(file.getName());
        fakeDocument.setTypeCode(Constants.BANKPROOF);
        fakeDocument.setShopId("1234");
        miraklShopDocumentList.add(fakeDocument);

        GetAccountHolderResponse getAccountHolderResponse = new GetAccountHolderResponse();
        BankAccountDetail bankAccountDetail = new BankAccountDetail();
        bankAccountDetail.setIban("ibansomething");
        bankAccountDetail.setBankAccountUUID("uuid");
        AccountHolderDetails accountHolderDetails = new AccountHolderDetails();
        accountHolderDetails.addBankAccountDetail(bankAccountDetail);
        getAccountHolderResponse.setAccountHolderDetails(accountHolderDetails);

        when(miraklMarketplacePlatformOperatorApiClientMock.getShopDocuments(any())).thenReturn(miraklShopDocumentList);
        when(miraklMarketplacePlatformOperatorApiClientMock.downloadShopsDocuments(any())).thenReturn(fileWrapper);
        when(fileWrapper.getFile()).thenReturn(file);
        when(fileWrapper.getFilename()).thenReturn(file.getName());
        when(adyenAccountServiceMock.getAccountHolder(any())).thenReturn(getAccountHolderResponse);
        when(adyenAccountServiceMock.uploadDocument(uploadDocumentRequestCaptor.capture())).thenReturn(null);

        docService.retrieveBankproofAndUpload();

        UploadDocumentRequest uploadDocumentRequest = uploadDocumentRequestCaptor.getValue();
        assertEquals("1234", uploadDocumentRequest.getAccountHolderCode());
        assertEquals("uuid", uploadDocumentRequest.getBankAccountUUID());
        assertEquals(file.getName(), uploadDocumentRequest.getDocumentDetail().getFilename());
        assertEquals(Base64.getEncoder().encodeToString(toByteArray(file)), uploadDocumentRequest.getDocumentContent());
        assertEquals(DocumentDetail.DocumentTypeEnum.BANK_STATEMENT, uploadDocumentRequest.getDocumentDetail().getDocumentType());
        verify(deltaService).getDocumentDelta();
    }

}
