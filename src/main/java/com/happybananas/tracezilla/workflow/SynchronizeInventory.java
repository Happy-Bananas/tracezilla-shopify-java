package com.happybananas.tracezilla.workflow;
import java.util.*;
public final class SynchronizeInventory {
 public record Source(String sku,double traceable,double nonTraceable,double defaultConversion,double nonTraceableConversion){}
 public record Target(String sku,String inventoryItemId,boolean tracked,Integer available){}
 public interface Reader{List<Source> read(int warehouse)throws Exception;}
 public interface Writer{Map<String,Target> read(String location)throws Exception;void write(Target item,int quantity,String location)throws Exception;}
 private final Reader source;private final Writer target;
 public SynchronizeInventory(Reader source,Writer target){this.source=source;this.target=target;}
 public Map<String,Object> run(String location,int warehouse,boolean dryRun,int limit)throws Exception{
  if(location.isBlank()||warehouse<1||limit<1)throw new IllegalArgumentException("Location, warehouse, and limit must be valid.");
  var destination=target.read(location);var items=new ArrayList<Map<String,Object>>();
  for(var inventory:source.read(warehouse).stream().limit(limit).toList()){
   var shopify=destination.get(inventory.sku());
   if(shopify==null){items.add(item(inventory.sku(),"skipped","No Shopify variant has this SKU.",null,null));continue;}
   if(!shopify.tracked()||shopify.available()==null){items.add(item(inventory.sku(),"skipped","Shopify does not track this item at the configured location.",null,null));continue;}
   try{var raw=inventory.traceable()*inventory.defaultConversion()+inventory.nonTraceable()*inventory.nonTraceableConversion();if(raw<0||raw!=Math.floor(raw)||raw>Integer.MAX_VALUE)throw new IllegalArgumentException("Mapped quantity must be a non-negative whole number.");var quantity=(int)raw;if(quantity==shopify.available())items.add(item(inventory.sku(),"unchanged","Quantity is already "+quantity+".",quantity,quantity));else if(dryRun)items.add(item(inventory.sku(),"would_update","Would change quantity from "+shopify.available()+" to "+quantity+".",shopify.available(),quantity));else{target.write(shopify,quantity,location);items.add(item(inventory.sku(),"updated","Changed quantity from "+shopify.available()+" to "+quantity+".",shopify.available(),quantity));}}catch(Exception error){items.add(item(inventory.sku(),"failed",error.getMessage(),null,null));}
  }
  var summary=new LinkedHashMap<String,Object>();summary.put("dry_run",dryRun);for(var status:List.of("updated","would_update","unchanged","skipped","failed"))summary.put(status,items.stream().filter(x->x.get("status").equals(status)).count());return Map.of("summary",summary,"items",items);
 }
 private static Map<String,Object> item(String sku,String status,String message,Integer from,Integer to){var value=new LinkedHashMap<String,Object>();value.put("sku",sku);value.put("status",status);value.put("message",message);value.put("from",from);value.put("to",to);return value;}
}
