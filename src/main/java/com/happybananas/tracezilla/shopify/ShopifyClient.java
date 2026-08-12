package com.happybananas.tracezilla.shopify;
import com.fasterxml.jackson.databind.*; import com.happybananas.tracezilla.Configuration;
import java.net.*; import java.net.http.*; import java.time.Duration; import java.util.*;

public final class ShopifyClient {
  private final Configuration config; private final HttpClient http; private final ObjectMapper json = new ObjectMapper(); private String token;
  public ShopifyClient(Configuration config) { this.config=config; this.http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(config.timeout())).build(); }
  public JsonNode graphql(String query, Map<String,Object> variables) throws Exception {
    var body=json.writeValueAsString(Map.of("query",query,"variables",variables)); var request=HttpRequest.newBuilder(URI.create("https://"+config.shopUrl()+"/admin/api/"+config.apiVersion()+"/graphql.json")).timeout(Duration.ofSeconds(config.timeout())).header("Content-Type","application/json").header("X-Shopify-Access-Token",accessToken()).POST(HttpRequest.BodyPublishers.ofString(body)).build();
    var response=http.send(request,HttpResponse.BodyHandlers.ofString()); if(response.statusCode()/100!=2) throw new IllegalStateException("Shopify request failed with HTTP "+response.statusCode()+"."); var payload=json.readTree(response.body()); if(payload.has("errors")&& !payload.get("errors").isEmpty()) throw new IllegalStateException("Shopify rejected the GraphQL query."); return payload;
  }
  private String accessToken() throws Exception { if(token!=null)return token; var form="grant_type=client_credentials&client_id="+enc(config.clientId())+"&client_secret="+enc(config.clientSecret())+"&scope="+enc(config.scope()); var request=HttpRequest.newBuilder(URI.create("https://"+config.shopUrl()+"/admin/oauth/access_token")).timeout(Duration.ofSeconds(config.timeout())).header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form)).build(); var response=http.send(request,HttpResponse.BodyHandlers.ofString()); if(response.statusCode()/100!=2)throw new IllegalStateException("Shopify authentication failed with HTTP "+response.statusCode()+"."); token=json.readTree(response.body()).path("access_token").asText(""); if(token.isBlank())throw new IllegalStateException("Shopify authentication did not return an access token."); return token; }
  private static String enc(String value){return URLEncoder.encode(value,java.nio.charset.StandardCharsets.UTF_8);}
}
