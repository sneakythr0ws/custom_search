package org.nick.util.customsearch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WarehouseDTO {
    @JsonProperty("beforeSpecifiedTime")
    public boolean beforeSpecifiedTime;
    @JsonProperty("commitDay")
    public int commitDay;
    @JsonProperty("company")
    public String company;
    @JsonProperty("companyDisplayName")
    public String companyDisplayName;
    @JsonProperty("currency")
    public String currency;
    @JsonProperty("deliveryDate")
    public String deliveryDate;
    @JsonProperty("discount")
    public int discount;
    @JsonProperty("discountType")
    public String discountType;
    @JsonProperty("domesticFreight")
    public String domesticFreight;
    @JsonProperty("errorCode")
    public int errorCode;
    @JsonProperty("errorDisplayMsg")
    public String errorDisplayMsg;
    @JsonProperty("isCheapestFaster")
    public boolean isCheapestFaster;
    @JsonProperty("isDefault")
    public boolean isDefault;
    @JsonProperty("isPromote")
    public boolean isPromote;
    @JsonProperty("isTracked")
    public boolean isTracked;
    @JsonProperty("localCurrency")
    public String localCurrency;
    @JsonProperty("localPrice")
    public String localPrice;
    @JsonProperty("localPriceFormatStr")
    public String localPriceFormatStr;
    @JsonProperty("localSaveMoney")
    public String localSaveMoney;
    @JsonProperty("localSaveMoneyFormatStr")
    public String localSaveMoneyFormatStr;
    @JsonProperty("localTotalFreight")
    public String localTotalFreight;
    @JsonProperty("localTotalFreightFormatStr")
    public String localTotalFreightFormatStr;
    @JsonProperty("logisticsDeliveryTimeType")
    public String logisticsDeliveryTimeType;
    @JsonProperty("price")
    public double price;
    @JsonProperty("priceFormatStr")
    public String priceFormatStr;
    @JsonProperty("processingTime")
    public int processingTime;
    @JsonProperty("promoteInformation")
    public String promoteInformation;
    @JsonProperty("saveMoney")
    public String saveMoney;
    @JsonProperty("saveMoneyFormatStr")
    public String saveMoneyFormatStr;
    @JsonProperty("sendGoodsCountry")
    public String sendGoodsCountry;
    @JsonProperty("sendGoodsCountryFullName")
    public String sendGoodsCountryFullName;
    @JsonProperty("status")
    public String status;
    @JsonProperty("templateDiscount")
    public String templateDiscount;
    @JsonProperty("templateType")
    public String templateType;
    @JsonProperty("time")
    public String time;
    @JsonProperty("totalFreight")
    public String totalFreight;
    @JsonProperty("totalFreightFormatStr")
    public String totalFreightFormatStr;
}
