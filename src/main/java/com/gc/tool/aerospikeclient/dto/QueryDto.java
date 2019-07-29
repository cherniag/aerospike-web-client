package com.gc.tool.aerospikeclient.dto;

import com.aerospike.client.query.IndexCollectionType;
import lombok.Value;

@Value
public class QueryDto {
    String key;
    String bin;
    QueryOperator queryOperator;
    IndexCollectionType collectionType;
    String value;
}
