package com.gc.tool.aerospikeclient.dto;

import lombok.Value;

@Value
public class CreateRecordDto {
    String key;
    String bins;
    int expiration;
    boolean sendKey;
}
