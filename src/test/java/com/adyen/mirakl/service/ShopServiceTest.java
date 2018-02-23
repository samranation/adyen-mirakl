package com.adyen.mirakl.service;

import com.adyen.mirakl.startup.MiraklStartupValidator;
import com.adyen.model.Name;
import com.adyen.model.marketpay.*;
import com.adyen.service.Account;
import com.google.common.collect.ImmutableList;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.common.currency.MiraklIsoCurrencyCode;
import com.mirakl.client.mmp.domain.shop.MiraklContactInformation;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.domain.shop.MiraklShops;
import com.mirakl.client.mmp.domain.shop.bank.MiraklIbanBankAccountInformation;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.request.shop.MiraklGetShopsRequest;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ShopServiceTest {
    @InjectMocks
    private ShopService shopService;

    @Mock
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClientMock;
    @Mock
    private Account adyenAccountServiceMock;
    @Mock
    private GetAccountHolderResponse getAccountHolderResponseMock;

    @Captor
    private ArgumentCaptor<CreateAccountHolderRequest> createAccountHolderRequestCaptor;
    @Captor
    private ArgumentCaptor<UpdateAccountHolderRequest> updateAccountHolderRequestCaptor;
    @Captor
    private ArgumentCaptor<MiraklGetShopsRequest> miraklGetShopsRequestCaptor;

    @Test
    public void testRetrieveUpdatedShopsZeroShops() throws Exception {
        MiraklShops miraklShops = new MiraklShops();
        List<MiraklShop> shops = new ArrayList<>();
        miraklShops.setShops(shops);
        miraklShops.setTotalCount(0L);

        when(miraklMarketplacePlatformOperatorApiClientMock.getShops(any())).thenReturn(miraklShops);

        shopService.retrieveUpdatedShops();
        verify(adyenAccountServiceMock, never()).createAccountHolder(any());
    }

    @Test
    public void testRetrieveUpdatedShopsCreate() throws Exception {
        MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue additionalField = new MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue();
        additionalField.setCode(String.valueOf(MiraklStartupValidator.CustomMiraklFields.ADYEN_LEGAL_ENTITY_TYPE));
        additionalField.setValue(MiraklStartupValidator.AdyenLegalEntityType.INDIVIDUAL.toString());
        setup(MiraklStartupValidator.AdyenLegalEntityType.INDIVIDUAL, ImmutableList.of(additionalField));
        when(adyenAccountServiceMock.createAccountHolder(createAccountHolderRequestCaptor.capture())).thenReturn(null);
        when(getAccountHolderResponseMock.getAccountHolderCode()).thenReturn("");

        shopService.retrieveUpdatedShops();
        CreateAccountHolderRequest request = createAccountHolderRequestCaptor.getValue();

        verify(adyenAccountServiceMock).createAccountHolder(request);

        assertEquals("id", request.getAccountHolderCode());
        assertEquals(CreateAccountHolderRequest.LegalEntityEnum.INDIVIDUAL, request.getLegalEntity());
        assertNotNull(request.getAccountHolderDetails().getIndividualDetails());
        IndividualDetails individualDetails = request.getAccountHolderDetails().getIndividualDetails();
        assertEquals("firstName", individualDetails.getName().getFirstName());
        assertEquals("lastName", individualDetails.getName().getLastName());
        assertEquals(Name.GenderEnum.FEMALE, individualDetails.getName().getGender());
    }

    @Test
    public void testRetrieveUpdatedShopsUpdate() throws Exception {
        MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue additionalField = new MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue();
        additionalField.setCode(String.valueOf(MiraklStartupValidator.CustomMiraklFields.ADYEN_LEGAL_ENTITY_TYPE));
        additionalField.setValue(MiraklStartupValidator.AdyenLegalEntityType.INDIVIDUAL.toString());
        setup(MiraklStartupValidator.AdyenLegalEntityType.INDIVIDUAL, ImmutableList.of(additionalField));
        when(adyenAccountServiceMock.updateAccountHolder(updateAccountHolderRequestCaptor.capture())).thenReturn(null);
        when(getAccountHolderResponseMock.getAccountHolderCode()).thenReturn("alreadyExisting");

        shopService.retrieveUpdatedShops();
        UpdateAccountHolderRequest request = updateAccountHolderRequestCaptor.getValue();

        verify(adyenAccountServiceMock).updateAccountHolder(request);

        assertEquals("id", request.getAccountHolderCode());
    }

    @Test
    public void testRetrieveUpdatedShopsPagination() throws Exception {
        //Response contains one shop and total_count = 2
        MiraklShops miraklShops = new MiraklShops();
        List<MiraklShop> shops = new ArrayList<>();
        miraklShops.setShops(shops);
        shops.add(new MiraklShop());
        miraklShops.setTotalCount(2L);

        when(miraklMarketplacePlatformOperatorApiClientMock.getShops(miraklGetShopsRequestCaptor.capture())).thenReturn(miraklShops);

        List<MiraklShop> updatedShops = shopService.getUpdatedShops();
        assertEquals(2, updatedShops.size());

        List<MiraklGetShopsRequest> miraklGetShopsRequests = miraklGetShopsRequestCaptor.getAllValues();
        assertEquals(2, miraklGetShopsRequests.size());
        assertEquals(0L, miraklGetShopsRequests.get(0).getOffset());
        assertEquals(1L, miraklGetShopsRequests.get(1).getOffset());
    }

    @Test
    public void testUpdateAccountHolderRequest() {
        MiraklShop shop = new MiraklShop();
        shop.setId("id");
        shop.setCurrencyIsoCode(MiraklIsoCurrencyCode.EUR);

        MiraklIbanBankAccountInformation miraklIbanBankAccountInformation = createMiraklIbanBankAccountInformation();
        shop.setPaymentInformation(miraklIbanBankAccountInformation);

        UpdateAccountHolderRequest request = shopService.updateAccountHolderRequestFromShop(shop);
        assertEquals("id", request.getAccountHolderCode());
        assertEquals("GB", request.getAccountHolderDetails().getBankAccountDetails().get(0).getCountryCode());
        assertEquals("Owner", request.getAccountHolderDetails().getBankAccountDetails().get(0).getOwnerName());
        assertEquals("GB00IBAN", request.getAccountHolderDetails().getBankAccountDetails().get(0).getIban());
        assertEquals("BIC", request.getAccountHolderDetails().getBankAccountDetails().get(0).getBankBicSwift());
        assertEquals("1111AA", request.getAccountHolderDetails().getBankAccountDetails().get(0).getOwnerPostalCode());
        assertEquals("BIC", request.getAccountHolderDetails().getBankAccountDetails().get(0).getBankBicSwift());
        assertEquals("1", request.getAccountHolderDetails().getBankAccountDetails().get(0).getOwnerHouseNumberOrName());
    }


    @Test
    public void shouldCreateBusinessAccount() throws Exception {
        MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue additionalField = new MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue();
        additionalField.setCode(String.valueOf(MiraklStartupValidator.CustomMiraklFields.ADYEN_LEGAL_ENTITY_TYPE));
        additionalField.setValue(MiraklStartupValidator.AdyenLegalEntityType.BUSINESS.toString());


        List<MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue> ubo1 = createMiraklAdditionalUboField("1");
        List<MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue> ubo2 = createMiraklAdditionalUboField("2");
        List<MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue> ubo3 = createMiraklAdditionalUboField("3");
        List<MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue> ubo4 = createMiraklAdditionalUboField("4");


        List<MiraklAdditionalFieldValue> addtionalFields = ImmutableList.of(ubo1, ubo2, ubo3, ubo4, ImmutableList.of(additionalField)).stream().flatMap(Collection::stream).collect(Collectors.toList());
        setup(MiraklStartupValidator.AdyenLegalEntityType.BUSINESS, addtionalFields);

        when(adyenAccountServiceMock.createAccountHolder(createAccountHolderRequestCaptor.capture())).thenReturn(null);
        when(getAccountHolderResponseMock.getAccountHolderCode()).thenReturn("");

        shopService.retrieveUpdatedShops();

        List<ShareholderContact> shareHolders = createAccountHolderRequestCaptor.getAllValues().stream()
            .map(CreateAccountHolderRequest::getAccountHolderDetails)
            .map(AccountHolderDetails::getBusinessDetails)
            .map(BusinessDetails::getShareholders)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());

        Set<String> firstNames = shareHolders.stream().map(ShareholderContact::getName).map(Name::getFirstName).collect(Collectors.toSet());
        Set<String> lastNames = shareHolders.stream().map(ShareholderContact::getName).map(Name::getLastName).collect(Collectors.toSet());
        Set<Name.GenderEnum> genders = shareHolders.stream().map(ShareholderContact::getName).map(Name::getGender).collect(Collectors.toSet());
        Set<String> emails = shareHolders.stream().map(ShareholderContact::getEmail).collect(Collectors.toSet());

        Assertions.assertThat(firstNames).containsExactlyInAnyOrder("firstname1", "firstname2", "firstname3", "firstname4");
        Assertions.assertThat(lastNames).containsExactlyInAnyOrder("lastname1", "lastname2", "lastname3", "lastname4");
        Assertions.assertThat(emails).containsExactlyInAnyOrder("email1", "email2", "email3", "email4");
        Assertions.assertThat(genders).containsOnly(Name.GenderEnum.UNKNOWN);

    }

    private List<MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue> createMiraklAdditionalUboField(String uboNumber) {
        MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue additionalFieldUboCivility = new MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue();
        additionalFieldUboCivility.setCode("firstname"+uboNumber);
        additionalFieldUboCivility.setValue("adyen-ubo"+uboNumber+"-firstname");
        MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue additionalFieldUboFirstName = new MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue();
        additionalFieldUboFirstName.setCode("lastname"+uboNumber);
        additionalFieldUboFirstName.setValue("adyen-ubo"+uboNumber+"-lastname");
        MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue additionalFieldUboLastName = new MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue();
        additionalFieldUboLastName.setCode("gender"+uboNumber);
        additionalFieldUboLastName.setValue("adyen-ubo"+uboNumber+"-gender");
        MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue additionalFieldUboDob = new MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue();
        additionalFieldUboDob.setCode("email"+uboNumber);
        additionalFieldUboDob.setValue("adyen-ubo"+uboNumber+"-email");
        return ImmutableList.of(additionalFieldUboCivility, additionalFieldUboFirstName, additionalFieldUboLastName, additionalFieldUboDob);
    }

    private MiraklIbanBankAccountInformation createMiraklIbanBankAccountInformation() {
        MiraklIbanBankAccountInformation miraklIbanBankAccountInformation = new MiraklIbanBankAccountInformation();

        miraklIbanBankAccountInformation.setOwner("Owner");
        miraklIbanBankAccountInformation.setIban("GB00IBAN");
        miraklIbanBankAccountInformation.setBic("BIC");
        miraklIbanBankAccountInformation.setBankZip("1111AA");
        miraklIbanBankAccountInformation.setBankStreet("1 street");

        return miraklIbanBankAccountInformation;
    }

    private void setup(MiraklStartupValidator.AdyenLegalEntityType type, List<MiraklAdditionalFieldValue> additionalFields) throws Exception {
        MiraklShops miraklShops = new MiraklShops();
        List<MiraklShop> shops = new ArrayList<>();
        miraklShops.setShops(shops);
        miraklShops.setTotalCount(1L);

        MiraklShop shop = new MiraklShop();
        shops.add(shop);

        MiraklContactInformation contactInformation = new MiraklContactInformation();
        contactInformation.setEmail("email");
        contactInformation.setFirstname("firstName");
        contactInformation.setLastname("lastName");
        contactInformation.setCivility("Mrs");
        shop.setContactInformation(contactInformation);

        shop.setAdditionalFieldValues(additionalFields);
        shop.setId("id");
        shop.setCurrencyIsoCode(MiraklIsoCurrencyCode.EUR);

        MiraklIbanBankAccountInformation miraklIbanBankAccountInformation = createMiraklIbanBankAccountInformation();
        shop.setPaymentInformation(miraklIbanBankAccountInformation);

        when(miraklMarketplacePlatformOperatorApiClientMock.getShops(any())).thenReturn(miraklShops);
        when(adyenAccountServiceMock.getAccountHolder(any())).thenReturn(getAccountHolderResponseMock);
    }
}
