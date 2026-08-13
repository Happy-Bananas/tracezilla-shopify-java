package com.happybananas.tracezilla;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.happybananas.tracezilla.shopify.*;
import com.happybananas.tracezilla.tracezilla.TracezillaCatalog;
import com.happybananas.tracezilla.workflow.*;
import java.util.*;
import java.util.stream.Collectors;

public final class Main {
  private Main() {}
  public static void main(String[] args) {
    try {
      var arguments=Arrays.asList(args); var json=arguments.contains("--json");
      var limit=Arrays.stream(args).filter(v->v.startsWith("--limit=")).findFirst().map(v->Integer.parseInt(v.substring(8))).orElse(10);
      var config=Configuration.fromEnvironment(); var client=new ShopifyClient(config);
      if(arguments.contains("synchronize-inventory")){var location=argument(args,"shopify-location");var warehouse=Integer.parseInt(argument(args,"tracezilla-warehouse"));var execute=arguments.contains("--execute");if(execute&&!arguments.contains("--confirm"))throw new IllegalArgumentException("Execution requires both --execute and --confirm.");var result=new SynchronizeInventory(new InventoryServices.Tracezilla(config),new InventoryServices.Shopify(client)).run(location,warehouse,!execute,limit);System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(result));return;}
      if(arguments.contains("list-shopify-locations")){var locations=new ShopifyLocationService(client).read();if(json)System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(locationResult(locations)));else renderLocations(locations);return;}
      var shopify=new ShopifyCatalog(client); var tracezilla=new TracezillaCatalog(config);
      if(arguments.contains("create-tracezilla-skus")){var execute=arguments.contains("--execute");if(execute&&!arguments.contains("--confirm"))throw new IllegalArgumentException("Execution requires both --execute and --confirm.");System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(new CreateTracezillaSkus(shopify,tracezilla).run(!execute,limit)));return;}
      var result=new CompareCatalogs(shopify,tracezilla).run(limit);
      if(json)System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(Map.of("status",result.status(),"display_limit",result.displayLimit(),"matched_count",result.presentInBoth().size(),"only_in_shopify_count",result.onlyInShopify().size(),"only_in_tracezilla_count",result.onlyInTracezilla().size(),"present_in_both",result.presentInBoth(),"only_in_shopify",result.onlyInShopify(),"only_in_tracezilla",result.onlyInTracezilla())));else render(result);
    } catch(Exception error){System.err.println("Command failed: "+error.getMessage());System.exit(1);}
  }
  private static void render(CompareCatalogs.Result r){System.out.printf("%-24s %-10s %-12s %s%n","SKU","Shopify","tracezilla","Result");System.out.println("-".repeat(72));var rows=new ArrayList<String[]>();r.presentInBoth().stream().limit(r.displayLimit()).forEach(s->rows.add(new String[]{s,"Yes","Yes","Match"}));r.onlyInShopify().stream().limit(r.displayLimit()).forEach(s->rows.add(new String[]{s,"Yes","No","Missing in tracezilla"}));r.onlyInTracezilla().stream().limit(r.displayLimit()).forEach(s->rows.add(new String[]{s,"No","Yes","Missing in Shopify"}));rows.stream().sorted(Comparator.comparing(a->a[0])).forEach(a->System.out.printf("%-24s %-10s %-12s %s%n",(Object[])a));System.out.printf("%nMatched: %d; missing in tracezilla: %d; missing in Shopify: %d%nShowing at most %d rows from each result category.%n",r.presentInBoth().size(),r.onlyInShopify().size(),r.onlyInTracezilla().size(),r.displayLimit());}
  private static void renderLocations(List<ShopifyLocation> locations){System.out.printf("%-24s %-9s %-10s %-13s %-22s %s%n","Name","Status","Inventory","Online orders","Legacy ID","GraphQL ID");System.out.println("-".repeat(112));for(var x:locations){System.out.printf("%-24s %-9s %-10s %-13s %-22s %s%n",x.name(),x.isActive()?"Active":"Inactive",x.hasActiveInventory()?"Yes":"No",x.fulfillsOnlineOrders()?"Yes":"No",x.legacyId(),x.graphQlId());var a=x.address();var city=java.util.stream.Stream.of(a.get("zip"),a.get("city")).filter(Objects::nonNull).collect(Collectors.joining(" "));var address=java.util.stream.Stream.of(a.get("address1"),a.get("address2"),city,a.get("province"),a.get("country")).filter(v->v!=null&&!v.isBlank()).collect(Collectors.joining(", "));System.out.println("Address: "+(address.isBlank()?"—":address));}System.out.printf("%n%d location(s) returned.%n",locations.size());if(locations.isEmpty())System.out.println("No Shopify locations are available to this app.");}
  private static Map<String,Object> locationResult(List<ShopifyLocation> locations){var items=locations.stream().map(x->{var item=new LinkedHashMap<String,Object>();item.put("graph_ql_id",x.graphQlId());item.put("legacy_id",x.legacyId());item.put("name",x.name());item.put("is_active",x.isActive());item.put("has_active_inventory",x.hasActiveInventory());item.put("fulfills_online_orders",x.fulfillsOnlineOrders());item.put("address",x.address());return item;}).toList();return Map.of("count",locations.size(),"locations",items);}
  private static String argument(String[] args,String name){return Arrays.stream(args).filter(x->x.startsWith("--"+name+"=")).findFirst().map(x->x.substring(name.length()+3)).orElse("");}
}
