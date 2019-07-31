package com.gc.tools.aerospikeclient.dto;

import lombok.Value;

@Value
public class AerospikeSet {
    String name;
    long recordCount;
}
