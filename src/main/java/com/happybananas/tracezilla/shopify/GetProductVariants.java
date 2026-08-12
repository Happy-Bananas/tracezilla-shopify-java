package com.happybananas.tracezilla.shopify;
public final class GetProductVariants { private GetProductVariants() {} public static final String DOCUMENT = """
query GetProductVariants($first: Int!, $after: String) {
  productVariants(first: $first, after: $after) {
    nodes { id sku displayName }
    pageInfo { hasNextPage endCursor }
  }
}
"""; }
