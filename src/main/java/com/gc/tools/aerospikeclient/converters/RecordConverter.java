package com.gc.tools.aerospikeclient.converters;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gc.tools.aerospikeclient.dto.AerospikeRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RecordConverter {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Bin[] EMPTY_BINS = {};
    private static final String NEVER = "never";
    private final ObjectMapper clientObjectMapper;

    public AerospikeRecord toAerospikeRecord(Key key, Record record) {
        log.debug("to record key {} digest {} record {}", key, Arrays.toString(key.digest), record);
        try {
            return record == null ? null : new AerospikeRecord(
                key.userKey == null ? null : key.userKey.toString(),
                HexUtils.toHexString(key.digest),
                record.generation,
                record.expiration == 0 ? NEVER : formatter.format(Date.from(Instant.now().plusSeconds(record.getTimeToLive()))),
                record.bins,
                clientObjectMapper.writeValueAsString(record.bins)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public Bin[] getBins(String raw) {
        if (raw == null || raw.isEmpty()) {
            return EMPTY_BINS;
        }
        Map<String, Object> content;
        try {
            content = clientObjectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content.entrySet().stream()
            .map(entry -> new Bin(entry.getKey(), entry.getValue()))
            .toArray(Bin[]::new);
    }
}
