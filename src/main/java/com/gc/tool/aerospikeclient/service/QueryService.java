package com.gc.tool.aerospikeclient.service;

import java.util.Arrays;
import java.util.Collections;
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
import com.aerospike.client.policy.QueryPolicy;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.Filter;
import com.aerospike.client.query.RecordSet;
import com.aerospike.client.query.Statement;
import com.gc.tool.aerospikeclient.converters.RecordConverter;
import com.gc.tool.aerospikeclient.dto.AerospikeRecord;
import com.gc.tool.aerospikeclient.dto.AerospikeSet;
import com.gc.tool.aerospikeclient.dto.CreateRecordDto;
import com.gc.tool.aerospikeclient.dto.QueryDto;
import com.gc.tool.aerospikeclient.dto.UpdateRecordDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryService {
    private static final int DO_NOT_CHANGE_EXPIRATION = -2;
    private final ClientProvider clientProvider;
    private final WritePolicy defaultUpdatePolicy;
    private final WritePolicy defaultCreatePolicy;
    private final WritePolicy defaultDeletePolicy;
    private final QueryPolicy defaultReadPolicy;
    private final RecordConverter recordConverter;

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
        log.info("getRecords connectionId {}, namespace {}, set {}", connectionId, namespace, set);
        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);
        Statement statement = new Statement();
        statement.setNamespace(namespace);
        statement.setSetName(set);
        return doQuery(aerospikeClient, statement);
    }

    public void deleteRecordByHash(Long connectionId, String namespace, String set, String hash) {
        log.info("deleteRecordByHash connectionId {}, namespace {}, set {}, key {}", connectionId, namespace, set, hash);

        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);
        Key key = new Key(namespace, HexUtils.fromHexString(hash), set, null);
        boolean existed = aerospikeClient.delete(defaultDeletePolicy, key);
        log.info("Record existed {}", existed);
    }

    public AerospikeRecord getRecordByHash(Long connectionId, String namespace, String set, String hash) {
        log.info("getRecord connectionId {}, namespace {}, set {}, hash {}", connectionId, namespace, set, hash);

        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);
        byte[] digest = HexUtils.fromHexString(hash);
        log.debug("Digest {}", Arrays.toString(digest));
        Key key = new Key(namespace, digest, set, null);
        Record record = aerospikeClient.get(defaultReadPolicy, key);

        return recordConverter.toAerospikeRecord(key, record);
    }

    public void updateRecord(Long connectionId, String namespace, String set, String hash, UpdateRecordDto updateDto) {
        log.info("updateRecord connectionId {}, namespace {}, set {}, hash {}, record {}", connectionId, namespace, set, hash, updateDto);

        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);

        Key key = new Key(namespace, HexUtils.fromHexString(hash), set, null);
        Bin[] bins = recordConverter.getBins(updateDto.getBins());

        WritePolicy policy = getWritePolicy(defaultUpdatePolicy, updateDto.getExpiration(), false, updateDto.getRecordExistsAction());
        policy.generationPolicy = updateDto.getGenerationPolicy();
        policy.generation = updateDto.getGeneration();
        aerospikeClient.put(policy, key, bins);
    }

    public void createRecord(Long connectionId, String namespace, String set, CreateRecordDto createDto) {
        log.info("createRecord connectionId {}, namespace {}, set {}, record {}", connectionId, namespace, set, createDto);

        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);

        Key key = new Key(namespace, set, createDto.getKey());
        Bin[] bins = recordConverter.getBins(createDto.getBins());

        WritePolicy policy = getWritePolicy(defaultCreatePolicy, createDto.getExpiration(), createDto.isSendKey(), createDto.getRecordExistsAction());
        aerospikeClient.put(policy, key, bins);
    }

    public List<AerospikeRecord> query(Long connectionId, String namespace, String set, QueryDto queryDto) {
        log.info("query connectionId {}, namespace {}, set {}, record {}", connectionId, namespace, set, queryDto);
        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);

        if (queryDto.getKey() != null && !queryDto.getKey().isEmpty()) {
            return Collections.singletonList(getRecordByKey(connectionId, namespace, set, queryDto.getKey()));
        }

        Filter filter = queryDto.getQueryOperator().getMapper().apply(queryDto);

        Statement statement = new Statement();
        statement.setNamespace(namespace);
        statement.setSetName(set);
        statement.setFilter(filter);
        return doQuery(aerospikeClient, statement);
    }

    private AerospikeRecord getRecordByKey(Long connectionId, String namespace, String set, String recordKey) {
        log.info("getRecordByKey connectionId {}, namespace {}, set {}, hash {}", connectionId, namespace, set, recordKey);

        AerospikeClient aerospikeClient = clientProvider.provide(connectionId);
        Key key = new Key(namespace, set, recordKey);
        Record record = aerospikeClient.get(defaultReadPolicy, key);

        return recordConverter.toAerospikeRecord(key, record);
    }

    private List<AerospikeRecord> doQuery(AerospikeClient aerospikeClient, Statement statement) {
        RecordSet recordSet = aerospikeClient.query(defaultReadPolicy, statement);
        Map<Key, Record> records = new HashMap<>();
        recordSet.forEach(keyRecord -> records.put(keyRecord.key, keyRecord.record));

        return records.entrySet().stream()
            .map(entry -> recordConverter.toAerospikeRecord(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    private WritePolicy getWritePolicy(WritePolicy defaultCreatePolicy, Integer expiration, boolean sendKey, RecordExistsAction recordExistsAction) {
        WritePolicy policy = new WritePolicy(defaultCreatePolicy);
        policy.expiration = expiration == null ? DO_NOT_CHANGE_EXPIRATION : expiration;
        policy.sendKey = sendKey;
        policy.recordExistsAction = recordExistsAction;
        return policy;
    }
}
