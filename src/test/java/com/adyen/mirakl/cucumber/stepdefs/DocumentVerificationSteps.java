package com.adyen.mirakl.cucumber.stepdefs;

import com.adyen.mirakl.cucumber.stepdefs.helpers.stepshelper.StepDefsHelper;
import com.adyen.model.marketpay.DocumentDetail;
import com.adyen.model.marketpay.GetUploadedDocumentsRequest;
import com.adyen.model.marketpay.GetUploadedDocumentsResponse;
import com.jayway.jsonpath.JsonPath;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import cucumber.api.DataTable;
import cucumber.api.java.en.And;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.Map;

import static com.adyen.mirakl.cucumber.stepdefs.helpers.hooks.CucumberHooks.cucumberMap;

public class DocumentVerificationSteps extends StepDefsHelper {
    @And("^the document is successfully uploaded to Adyen$")
    public void theDocumentIsSuccessfullyUploadedToAdyen(DataTable table) throws Throwable {
        List<Map<String, String>> cucumberTable = table.getTableConverter().toMaps(table, String.class, String.class);

        MiraklShop createdShop = (MiraklShop)cucumberMap.get("createdShop");
        GetUploadedDocumentsRequest getUploadedDocumentsRequest = new GetUploadedDocumentsRequest();
        getUploadedDocumentsRequest.setAccountHolderCode(createdShop.getId());
        GetUploadedDocumentsResponse uploadedDocuments = adyenAccountService.getUploadedDocuments(getUploadedDocumentsRequest);

        boolean documentTypeAndFilenameMatch = uploadedDocuments.getDocumentDetails().stream()
            .anyMatch(doc ->
                DocumentDetail.DocumentTypeEnum.valueOf(cucumberTable.get(0).get("documentType")).equals(doc.getDocumentType())
                && cucumberTable.get(0).get("filename").equals(doc.getFilename()));

        String uploadedDocResponse = uploadedDocuments.getDocumentDetails().toString();
        Assertions.assertThat(documentTypeAndFilenameMatch)
            .withFailMessage(String.format("Document upload response:[%s]", JsonPath.parse(uploadedDocResponse).toString()))
            .isTrue();
    }
}
