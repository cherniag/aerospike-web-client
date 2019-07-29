package com.gc.tool.aerospikeclient.dto;

import java.util.function.Function;

import com.aerospike.client.query.Filter;

public enum QueryOperator {
    EQUALS(queryDto -> Filter.equal(queryDto.getBin(), queryDto.getValue())),
    CONTAINS(queryDto -> Filter.contains(queryDto.getBin(), queryDto.getCollectionType(), queryDto.getValue()));

    private Function<QueryDto, Filter> mapper;

    QueryOperator(Function<QueryDto, Filter> mapper) {
        this.mapper = mapper;
    }

    public Function<QueryDto, Filter> getMapper() {
        return mapper;
    }
}
