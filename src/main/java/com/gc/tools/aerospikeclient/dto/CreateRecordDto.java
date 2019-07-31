package com.gc.tools.aerospikeclient.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.aerospike.client.policy.GenerationPolicy;
import com.aerospike.client.policy.RecordExistsAction;
import lombok.Value;

@Value
public class CreateRecordDto {
    @NotEmpty
    String key;
    Integer expiration;
    boolean sendKey;
    @NotNull
    RecordExistsAction recordExistsAction;
    @NotEmpty
    String bins;
}
