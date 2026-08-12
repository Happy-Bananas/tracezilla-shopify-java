package com.happybananas.tracezilla;

public record Configuration(String shopUrl, String clientId, String clientSecret, String scope,
    String apiVersion, String tracezillaUrl, String teamSlug, String apiKey, int timeout) {
  public static Configuration fromEnvironment() {
    var shop = required("SHOPIFY_SHOP_URL").replaceFirst("(?i)^https?://", "").replaceFirst("/$", "");
    if (!shop.endsWith(".myshopify.com") || shop.contains("/")) throw new IllegalArgumentException("SHOPIFY_SHOP_URL must look like your-store.myshopify.com.");
    int timeout;
    try { timeout = Integer.parseInt(required("HTTP_TIMEOUT")); } catch (NumberFormatException error) { throw new IllegalArgumentException("HTTP_TIMEOUT must be a positive integer.", error); }
    if (timeout < 1) throw new IllegalArgumentException("HTTP_TIMEOUT must be a positive integer.");
    return new Configuration(shop, required("SHOPIFY_CLIENT_ID"), required("SHOPIFY_CLIENT_SECRET"), required("SHOPIFY_SCOPE"), required("SHOPIFY_API_VERSION"), required("TRACEZILLA_BASE_URL").replaceFirst("/$", ""), required("TRACEZILLA_TEAM_SLUG"), required("TRACEZILLA_API_KEY"), timeout);
  }
  private static String required(String key) { var value = System.getenv(key); if (value == null || value.isBlank()) throw new IllegalArgumentException("Missing required configuration: " + key); return value.trim(); }
}
