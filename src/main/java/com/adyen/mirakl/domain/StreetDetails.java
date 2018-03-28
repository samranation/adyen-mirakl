package com.adyen.mirakl.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StreetDetails {
    private String streetName;
    private String houseNumberOrName;

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public static StreetDetails createStreetDetailsFromSingleLine(String street, Pattern houseNumberPattern) {
        StreetDetails streetDetails = new StreetDetails();
        if(street == null) {
            return streetDetails;
        }

        Matcher matcher = houseNumberPattern.matcher(street);
        if (matcher.find()) {
            String houseNumber = matcher.group(1);
            StringBuilder sb = new StringBuilder();
            sb.append(street.substring(0, matcher.start()));
            if (matcher.end() + 1 < street.length()) {
                sb.append(street.substring(matcher.end()));
            }
            streetDetails.setStreetName(sb.toString());
            streetDetails.setHouseNumberOrName(houseNumber);
        } else {
            streetDetails.setStreetName(street);
        }

        return streetDetails;
    }

    public String getHouseNumberOrName() {
        return houseNumberOrName;
    }

    public void setHouseNumberOrName(String houseNumberOrName) {
        this.houseNumberOrName = houseNumberOrName;
    }
}
