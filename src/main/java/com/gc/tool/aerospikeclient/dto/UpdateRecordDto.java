package com.gc.tool.aerospikeclient.dto;

import com.aerospike.client.policy.GenerationPolicy;
import com.aerospike.client.policy.RecordExistsAction;
import lombok.Value;

@Value
public class UpdateRecordDto {
    Integer expiration;
    int generation;
    boolean sendKey;
    RecordExistsAction recordExistsAction;
    GenerationPolicy generationPolicy;
    String bins;
}
