package com.adyen.mirakl.service;

import com.adyen.mirakl.startup.MiraklStartupValidator;
import com.adyen.model.Name;
import com.adyen.model.marketpay.*;
import com.adyen.model.marketpay.CreateAccountHolderRequest.LegalEntityEnum;
import com.adyen.service.Account;
import com.adyen.service.exception.ApiException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mirakl.client.mmp.domain.additionalfield.MiraklAdditionalFieldType;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue;
import com.mirakl.client.mmp.domain.common.MiraklAdditionalFieldValue.MiraklValueListAdditionalFieldValue;
import com.mirakl.client.mmp.domain.shop.MiraklContactInformation;
import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.domain.shop.MiraklShops;
import com.mirakl.client.mmp.domain.shop.bank.MiraklIbanBankAccountInformation;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.request.shop.MiraklGetShopsRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Transactional
public class ShopService {
    private static final String ADYEN_UBO = "adyen-ubo";
    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String CIVILITY = "civility";
    private static final String EMAIL = "email";
    private final Logger log = LoggerFactory.getLogger(ShopService.class);

    private static Map<String, Name.GenderEnum> CIVILITY_TO_GENDER = ImmutableMap.<String, Name.GenderEnum>builder().put("Mr", Name.GenderEnum.MALE)
                                                                                                                    .put("Mrs", Name.GenderEnum.FEMALE)
                                                                                                                    .put("Miss", Name.GenderEnum.FEMALE)
                                                                                                                    .build();

    @Resource
    private MiraklMarketplacePlatformOperatorApiClient miraklMarketplacePlatformOperatorApiClient;

    @Resource
    private Account adyenAccountService;

    @Value("${shopService.maxUbos}")
    private Integer maxUbos = 4;

    @Scheduled(cron = "${application.shopUpdaterCron}")
    public void retrieveUpdatedShops() {
        List<MiraklShop> shops = getUpdatedShops();

        log.debug("Retrieved shops: " + shops.size());
        for (MiraklShop shop : shops) {
            try {
                if (accountHolderExists(shop, adyenAccountService)) {
                    UpdateAccountHolderRequest updateAccountHolderRequest = updateAccountHolderRequestFromShop(shop);
                    UpdateAccountHolderResponse response = adyenAccountService.updateAccountHolder(updateAccountHolderRequest);
                    log.debug("UpdateAccountHolderResponse: " + response);
                } else {
                    CreateAccountHolderRequest createAccountHolderRequest = createAccountHolderRequestFromShop(shop);
                    CreateAccountHolderResponse response = adyenAccountService.createAccountHolder(createAccountHolderRequest);
                    log.debug("CreateAccountHolderResponse: " + response);
                }
            } catch (ApiException e) {
                // account does not exists yet
                log.warn("MarketPay Api Exception: " + e.getError());
            } catch (Exception e) {
                log.warn("Exception: " + e.getMessage());
            }
        }
    }

    public List<MiraklShop> getUpdatedShops() {
        int offset = 0;
        Long totalCount = 1L;
        List<MiraklShop> shops = new ArrayList<>();

        while (offset < totalCount) {
            MiraklGetShopsRequest miraklGetShopsRequest = new MiraklGetShopsRequest();
            miraklGetShopsRequest.setPaginate(false);
            miraklGetShopsRequest.setOffset(offset);

            MiraklShops miraklShops = miraklMarketplacePlatformOperatorApiClient.getShops(miraklGetShopsRequest);
            shops.addAll(miraklShops.getShops());

            totalCount = miraklShops.getTotalCount();
            offset += miraklShops.getShops().size();
        }

        return shops;
    }

    private CreateAccountHolderRequest createAccountHolderRequestFromShop(MiraklShop shop) {
        CreateAccountHolderRequest createAccountHolderRequest = new CreateAccountHolderRequest();

        // Set Account holder code
        createAccountHolderRequest.setAccountHolderCode(shop.getId());

        // Set LegalEntity
        LegalEntityEnum legalEntity = getLegalEntityFromShop(shop);
        createAccountHolderRequest.setLegalEntity(legalEntity);

        // Set AccountHolderDetails
        AccountHolderDetails accountHolderDetails = new AccountHolderDetails();

        if (LegalEntityEnum.INDIVIDUAL.equals(legalEntity)) {
            IndividualDetails individualDetails = createIndividualDetailsFromShop(shop);
            accountHolderDetails.setIndividualDetails(individualDetails);
        } else if (LegalEntityEnum.BUSINESS.equals(legalEntity)) {
            BusinessDetails businessDetails = createBusinessDetailsFromShop(shop);
            accountHolderDetails.setBusinessDetails(businessDetails);
        }else{
            throw new IllegalArgumentException(legalEntity.toString() + " not supported");
        }

        // Set email
        MiraklContactInformation contactInformation = getContactInformationFromShop(shop);
        accountHolderDetails.setEmail(contactInformation.getEmail());
        createAccountHolderRequest.setAccountHolderDetails(accountHolderDetails);

        return createAccountHolderRequest;
    }

    private LegalEntityEnum getLegalEntityFromShop(MiraklShop shop) {
        MiraklValueListAdditionalFieldValue additionalFieldValue = (MiraklValueListAdditionalFieldValue) shop.getAdditionalFieldValues()
                                                                                                             .stream()
                                                                                                             .filter(field -> isListWithCode(field,
                                                                                                                                             MiraklStartupValidator.CustomMiraklFields.ADYEN_LEGAL_ENTITY_TYPE))
                                                                                                             .findAny()
                                                                                                             .orElseThrow(() -> new RuntimeException("Legal entity not found"));

        LegalEntityEnum legalEntity = Arrays.stream(LegalEntityEnum.values())
                                            .filter(legalEntityEnum -> legalEntityEnum.toString().equalsIgnoreCase(additionalFieldValue.getValue()))
                                            .findAny()
                                            .orElseThrow(() -> new RuntimeException("Invalid legal entity: " + additionalFieldValue.toString()));

        return legalEntity;
    }

    private MiraklContactInformation getContactInformationFromShop(MiraklShop shop) {
        return Optional.of(shop.getContactInformation()).orElseThrow(() -> new RuntimeException("Contact information not found"));
    }


    private BusinessDetails createBusinessDetailsFromShop(final MiraklShop shop) {
        BusinessDetails businessDetails = new BusinessDetails();
        businessDetails.setShareholders(extractUbos(shop));
        return businessDetails;
    }

    private IndividualDetails createIndividualDetailsFromShop(MiraklShop shop) {
        IndividualDetails individualDetails = new IndividualDetails();

        MiraklContactInformation contactInformation = getContactInformationFromShop(shop);

        Name name = new Name();
        name.setFirstName(contactInformation.getFirstname());
        name.setLastName(contactInformation.getLastname());
        name.setGender(CIVILITY_TO_GENDER.getOrDefault(contactInformation.getCivility(), Name.GenderEnum.UNKNOWN));
        individualDetails.setName(name);
        return individualDetails;
    }

    private boolean isListWithCode(MiraklAdditionalFieldValue additionalFieldValue, MiraklStartupValidator.CustomMiraklFields field) {
        return MiraklAdditionalFieldType.LIST.equals(additionalFieldValue.getFieldType()) && field.toString().equalsIgnoreCase(additionalFieldValue.getCode());
    }

    /**
     * Check if AccountHolder already exists in Adyen
     */
    private boolean accountHolderExists(MiraklShop shop, Account adyenAccountService) throws Exception {

        // lookup accountHolder in Adyen
        GetAccountHolderRequest getAccountHolderRequest = new GetAccountHolderRequest();
        getAccountHolderRequest.setAccountHolderCode(shop.getId());

        try {
            GetAccountHolderResponse getAccountHolderResponse = adyenAccountService.getAccountHolder(getAccountHolderRequest);
            if (! getAccountHolderResponse.getAccountHolderCode().isEmpty()) {
                return true;
            }
        } catch (ApiException e) {
            // account does not exists yet
            log.debug("MarketPay Api Exception: " + e.getError());
        }

        return false;
    }

    /**
     * Construct updateAccountHolderRequest to Adyen from Mirakl shop
     */
    protected UpdateAccountHolderRequest updateAccountHolderRequestFromShop(MiraklShop shop) {
        UpdateAccountHolderRequest updateAccountHolderRequest = new UpdateAccountHolderRequest();
        updateAccountHolderRequest.setAccountHolderCode(shop.getId());

        // create AcountHolderDetails
        AccountHolderDetails accountHolderDetails = new AccountHolderDetails();
        updateAccountHolderRequest.setAccountHolderDetails(accountHolderDetails);

        if (shop.getPaymentInformation() instanceof MiraklIbanBankAccountInformation) {
            MiraklIbanBankAccountInformation miraklIbanBankAccountInformation = (MiraklIbanBankAccountInformation) shop.getPaymentInformation();
            if (! miraklIbanBankAccountInformation.getIban().isEmpty() && shop.getCurrencyIsoCode() != null) {
                // set BankAccountDetails
                BankAccountDetail bankAccountDetail = createBankAccountDetail(miraklIbanBankAccountInformation, shop.getCurrencyIsoCode().toString());
                List<BankAccountDetail> bankAccountDetails = new ArrayList<BankAccountDetail>();
                bankAccountDetails.add(bankAccountDetail);
                accountHolderDetails.setBankAccountDetails(bankAccountDetails);
            }
        }

        return updateAccountHolderRequest;
    }

    private BankAccountDetail createBankAccountDetail(MiraklIbanBankAccountInformation miraklIbanBankAccountInformation, String currencyCode) {
        // create AcountHolderDetails
        AccountHolderDetails accountHolderDetails = new AccountHolderDetails();

        // set BankAccountDetails
        BankAccountDetail bankAccountDetail = new BankAccountDetail();

        // check if PaymentInformation is object MiraklIbanBankAccountInformation
        miraklIbanBankAccountInformation.getIban();
        bankAccountDetail.setIban(miraklIbanBankAccountInformation.getIban());
        bankAccountDetail.setBankBicSwift(miraklIbanBankAccountInformation.getBic());
        bankAccountDetail.setCountryCode(getBankCountryFromIban(miraklIbanBankAccountInformation.getIban())); // required field
        bankAccountDetail.setCurrencyCode(currencyCode);

        bankAccountDetail.setOwnerPostalCode(miraklIbanBankAccountInformation.getBankZip());
        bankAccountDetail.setOwnerHouseNumberOrName(getHouseNumberFromStreet(miraklIbanBankAccountInformation.getBankStreet()));
        bankAccountDetail.setOwnerName(miraklIbanBankAccountInformation.getOwner());

        List<BankAccountDetail> bankAccountDetails = new ArrayList<BankAccountDetail>();
        bankAccountDetails.add(bankAccountDetail);
        accountHolderDetails.setBankAccountDetails(bankAccountDetails);

        return bankAccountDetail;
    }

    private List<ShareholderContact> extractUbos(final MiraklShop shop) {
        Map<String, String> extractedKeysFromMirakl = shop.getAdditionalFieldValues().stream()
            .filter(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue.class::isInstance)
            .map(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue.class::cast)
            .collect(Collectors.toMap(MiraklAdditionalFieldValue.MiraklAbstractAdditionalFieldWithSingleValue::getValue, MiraklAdditionalFieldValue::getCode));

        ImmutableList.Builder<ShareholderContact> builder = ImmutableList.builder();
        generateKeys().forEach((i, keys) -> {
            String firstName = extractedKeysFromMirakl.getOrDefault(keys.get(FIRSTNAME), "");
            String lastName = extractedKeysFromMirakl.getOrDefault(keys.get(LASTNAME), "");
            String civility = extractedKeysFromMirakl.getOrDefault(keys.get(CIVILITY), "");
            String email = extractedKeysFromMirakl.getOrDefault(keys.get(EMAIL), "");

            if(ImmutableList.of(firstName, lastName, civility, email).stream().noneMatch(StringUtils::isBlank)){
                ShareholderContact shareholderContact = new ShareholderContact();
                Name name = new Name();
                name.setFirstName(firstName);
                name.setLastName(lastName);
                name.setGender(CIVILITY_TO_GENDER.getOrDefault(civility, Name.GenderEnum.UNKNOWN));
                shareholderContact.setName(name);
                shareholderContact.setEmail(email);
                builder.add(shareholderContact);
            }
        });
        return builder.build();
    }

    private Map<Integer, Map<String, String>> generateKeys(){
        return  IntStream.rangeClosed(1, maxUbos)
            .mapToObj(i -> {
                final Map<Integer, Map<String, String>> grouped = new HashMap<>();
                grouped.put(i, ImmutableMap.of(
                    FIRSTNAME, ADYEN_UBO + String.valueOf(i) + "-firstname",
                    LASTNAME, ADYEN_UBO + String.valueOf(i) + "-lastname",
                    CIVILITY, ADYEN_UBO + String.valueOf(i) + "-civility",
                    EMAIL, ADYEN_UBO + String.valueOf(i) + "-email"));
                return grouped;
            }).reduce((x, y) -> {
                x.put(y.entrySet().iterator().next().getKey()
                    , y.entrySet().iterator().next().getValue());
                return x;
            }).orElseThrow(() -> new IllegalStateException("UBOs must exist, number found: " + maxUbos));
    }

    /**
     * First two digits of IBAN holds ISO country code
     */
    private String getBankCountryFromIban(String iban) {
        return iban.substring(0, 2);
    }


    /**
     * TODO: implement method to retrieve housenumber from street
     */
    private String getHouseNumberFromStreet(String street) {
        return "1";
    }

    public Integer getMaxUbos() {
        return maxUbos;
    }

    public void setMaxUbos(Integer maxUbos) {
        this.maxUbos = maxUbos;
    }
}
