package com.gc.tool.aerospikeclient.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.aerospike.client.policy.GenerationPolicy;
import com.aerospike.client.policy.RecordExistsAction;
import lombok.Value;

@Value
public class UpdateRecordDto {
    Integer expiration;
    @Positive
    int generation;
    @NotNull
    RecordExistsAction recordExistsAction;
    @NotNull
    GenerationPolicy generationPolicy;
    @NotEmpty
    String bins;
}
