package com.happybananas.tracezilla.shopify;
import java.util.Map;
public record ShopifyLocation(String graphQlId,String legacyId,String name,boolean isActive,boolean hasActiveInventory,boolean fulfillsOnlineOrders,Map<String,String> address) {}
