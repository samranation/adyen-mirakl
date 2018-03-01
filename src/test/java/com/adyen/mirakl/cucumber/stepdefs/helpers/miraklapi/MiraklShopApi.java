package com.adyen.mirakl.cucumber.stepdefs.helpers.miraklapi;

import com.mirakl.client.mmp.domain.shop.MiraklShop;
import com.mirakl.client.mmp.domain.shop.MiraklShops;
import com.mirakl.client.mmp.operator.core.MiraklMarketplacePlatformOperatorApiClient;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShopReturn;
import com.mirakl.client.mmp.operator.domain.shop.create.MiraklCreatedShops;
import com.mirakl.client.mmp.operator.request.shop.MiraklCreateShopsRequest;
import com.mirakl.client.mmp.request.shop.MiraklGetShopsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MiraklShopApi extends MiraklShopProperties {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public MiraklCreatedShops createNewShop(MiraklMarketplacePlatformOperatorApiClient client, Map tableData, boolean createShopHolderData) {

        MiraklCreateShopsRequest miraklShopRequest = createMiraklShopRequest(tableData, createShopHolderData);
        MiraklCreatedShops shops = client.createShops(miraklShopRequest);

        MiraklCreatedShopReturn miraklCreatedShopReturn = shops.getShopReturns()
                                                               .stream()
                                                               .findAny()
                                                               .orElseThrow(() -> new IllegalStateException("No Shop found"));

        if (miraklCreatedShopReturn.getShopCreated() == null) {
            throw new IllegalStateException(miraklCreatedShopReturn.getShopError().getErrors().toString());
        }
        String shopId = shops.getShopReturns().iterator().next().getShopCreated().getId();
        log.info(String.format("Mirakl Shop Id: [%s]", shopId));

        return shops;
    }

    private MiraklShops getAllMiraklShops(MiraklMarketplacePlatformOperatorApiClient client) {
        MiraklGetShopsRequest request = new MiraklGetShopsRequest();
        request.setPaginate(false);
        return client.getShops(request);
    }

    public MiraklShop filterMiraklShopsByEmailAndReturnShop(MiraklMarketplacePlatformOperatorApiClient client, String email) {
        MiraklShops shops = getAllMiraklShops(client);
        return shops.getShops()
            .stream().filter(shop -> shop.getContactInformation().getEmail().equalsIgnoreCase(email)).findAny()
            .orElseThrow(() -> new IllegalStateException("Shop cannot be found."));
    }
}
