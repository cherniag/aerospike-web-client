package com.gc.tool.aerospikeclient.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Info;
import com.aerospike.client.Key;
import com.aerospike.client.Record;
import com.aerospike.client.cluster.Node;
import com.aerospike.client.policy.Priority;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.ScanPolicy;
import com.aerospike.client.policy.WritePolicy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.stereotype.Service;

import com.gc.tool.aerospikeclient.dto.CreateRecordDto;
import com.gc.tool.aerospikeclient.dto.AerospikeRecord;
import com.gc.tool.aerospikeclient.dto.AerospikeSet;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryService {
    private final ClientProvider clientProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<String> getNameSpaces(Long connectionId) {
        log.info("getNameSpaces {}", connectionId);

        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);
        Node node = aerospikeClient.getNodes()[0];
        log.info("Aerospike node {} is active {}", node.getAddress(), node.isActive());

        String filter = "namespaces";
        String tokens = Info.request(null, node, filter);

        log.info("Tokens: {}", tokens);

        return Stream.of(tokens.split(";"))
            .collect(Collectors.toList());
    }

    public List<AerospikeSet> getSets(Long connectionId, String namespace) {
        log.info("getSets {}, namespace {}", connectionId, namespace);

        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);
        Node node = aerospikeClient.getNodes()[0];
        log.info("Aerospike node {} is active {}", node.getAddress(), node.isActive());

        String filter = "sets/" + namespace;
        String tokens = Info.request(null, node, filter);

        log.info("Tokens: {}", tokens);

        // ns=COMMON:set=clans-benefit-service-conf:objects=5:tombstones=0:memory_data_bytes=0:truncate_lut=0:stop-writes-count=0:set-enable-xdr=use-default:disable-eviction=false

        return Stream.of(tokens.split(";"))
            .filter(token -> !token.isEmpty())
            .map(token -> Stream.of(token.split(":"))
                .map(chunk -> chunk.split("="))
                .collect(Collectors.toMap(chunks -> chunks[0], chunks -> chunks[1])))
            .map(map -> new AerospikeSet(map.get("set"), Long.valueOf(map.get("objects"))))
            .collect(Collectors.toList());
    }

    public List<AerospikeRecord> getRecords(Long connectionId, String namespace, String set) {
        log.info("getRecords {}, namespace {}, set {}", connectionId, namespace, set);

        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);

        ScanPolicy policy = new ScanPolicy();
        policy.concurrentNodes = true;
        policy.priority = Priority.LOW;
        policy.includeBinData = true;

        Map<Key, Record> records = new HashMap<>();
        aerospikeClient.scanAll(policy, namespace, set, records::put);

        return records.entrySet().stream()
            .map(entry -> toAerospikeRecord(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    public void deleteRecord(Long connectionId, String namespace, String set, String keyToRemove) {
        log.info("deleteRecord {}, namespace {}, set {}, key {}", connectionId, namespace, set, keyToRemove);

        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);
        Key key = new Key(namespace, set, keyToRemove);
        boolean existed = aerospikeClient.delete(new WritePolicy(), key);
        log.info("Record existed {}", existed);
    }

    public AerospikeRecord getRecord(Long connectionId, String namespace, String set, String keyId) {
        log.info("getRecords {}, namespace {}, set {}", connectionId, namespace, set);

        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);
        Key key = new Key(namespace, set, keyId);
        Record record = aerospikeClient.get(new ScanPolicy(), key);

        return toAerospikeRecord(key, record);
    }

    public void updateRecord(Long connectionId, String namespace, String set, String keyId, AerospikeRecord aerospikeRecord) {
        log.info("updateRecord {}, namespace {}, set {}, raw {}", connectionId, namespace, set, aerospikeRecord.getRaw());

        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);

        Key key = new Key(namespace, set, keyId);
        Bin[] bins = getBins(aerospikeRecord.getRaw());

        WritePolicy policy = new WritePolicy();
        policy.recordExistsAction = RecordExistsAction.REPLACE_ONLY;
        aerospikeClient.put(policy, key, bins);
    }

    public void createRecord(Long connectionId, String namespace, String set, CreateRecordDto createRecordDto) {
        log.info("createRecord {}, namespace {}, set {}, record {}", connectionId, namespace, set, createRecordDto);

        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);

        Key key = new Key(namespace, set, createRecordDto.getKey());
        Bin[] bins = getBins(createRecordDto.getBins());

        WritePolicy policy = new WritePolicy();
        policy.recordExistsAction = RecordExistsAction.CREATE_ONLY;
        policy.expiration = createRecordDto.getExpiration();
        policy.sendKey = createRecordDto.isSendKey();
        aerospikeClient.put(policy, key, bins);
    }

    private Bin[] getBins(String raw) {
        Map<String, Object> content;
        try {
            content = objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content.entrySet().stream()
            .map(entry -> new Bin(entry.getKey(), entry.getValue()))
            .toArray(Bin[]::new);
    }

    private AerospikeRecord toAerospikeRecord(Key key, Record record) {
        try {
            return new AerospikeRecord(
                key.userKey == null ? null : key.userKey.toString(),
                HexUtils.toHexString(key.digest),
                record.generation,
                record.expiration,
                record.bins,
                objectMapper.writeValueAsString(record.bins)
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
