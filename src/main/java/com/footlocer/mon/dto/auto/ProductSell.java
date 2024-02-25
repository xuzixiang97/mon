package com.footlocer.mon.dto.auto;

import lombok.Data;

import java.util.List;

@Data
public class ProductSell {

    private String type;
    private List<String> publishHistory;
    private String country;
    private String city;
    private String priceType;
    private double stockxFee;
    private boolean sold;
    private int exactPrice;
    private boolean publishOnStockx;
    private boolean publishOnGoat;
    private boolean publishOnEbay;
    private int agencySaleStockxPrice;
    private int agencySaleGoatPrice;
    private int agencySaleEbayEarningFloor;
    private String agencySaleStockxProductId;
    private String agencySaleGoatProductId;
    private String agencySaleGoatExtTag;
    private String agencySaleStockxOrderId;
    private String agencySaleGoatOrderId;
    private String agencySaleEbayOrderId;
    private String touchStatus;
    private String agencySaleStockxStatus;
    private String agencySaleGoatStatus;
    private String agencySaleEbayStatus;
    private int securityDepositAmount;
    private boolean hasPaidSecurityDeposit;
    private String securityDepositStatus;
    private String erpName;
    private String remark;
    private boolean stockxIsAutoPricing;
    private boolean stockxIsPausePricing;
    private String stockxAutoPricingStrategy;
    private boolean shouldShowStockxAutoPricingSwitch;
    private boolean goatIsAutoPricing;
    private boolean goatIsPausePricing;
    private String goatAutoPricingStrategy;
    private boolean shouldShowGoatAutoPricingSwitch;
    private String boxCondition;
    private String _id;
    //private User user;
    private String name;
    private String singleGender;
    private String brandName;
    private String pictureUrl;
    private String sku;
    private String size;
    // private List<PriceHistory> priceHistory;
    // private List<PriceChangeHistory> stockxPriceChangeHistory;
    //private List<PriceChangeHistory> goatPriceChangeHistory;
    private String createdAt;
    private String updatedAt;
    private int __v;

}
