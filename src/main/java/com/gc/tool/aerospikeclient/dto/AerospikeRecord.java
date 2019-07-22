package com.gc.tool.aerospikeclient.dto;

import java.util.Map;

import lombok.Value;

@Value
public class AerospikeRecord {
    String key;
    String hash;
    Integer generation;
    int expiration;
    Map<String, Object> content;
    String raw;
}
