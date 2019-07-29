package com.gc.tool.aerospikeclient.controllers;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.aerospike.client.policy.GenerationPolicy;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.query.IndexCollectionType;
import com.gc.tool.aerospikeclient.dto.AerospikeRecord;
import com.gc.tool.aerospikeclient.dto.CreateRecordDto;
import com.gc.tool.aerospikeclient.dto.QueryDto;
import com.gc.tool.aerospikeclient.dto.QueryOperator;
import com.gc.tool.aerospikeclient.dto.UpdateRecordDto;
import com.gc.tool.aerospikeclient.service.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/record")
@Slf4j
@RequiredArgsConstructor
public class RecordController {
    private final QueryService queryService;

    @GetMapping("/{connectionId}/{namespace}/{set}")
    public String getRecordPage(@PathVariable("connectionId") Long connectionId,
                                @PathVariable("namespace") String namespace,
                                @PathVariable("set") String set,
                                Model model) {
        model.addAttribute("connectionId", connectionId);
        model.addAttribute("namespace", namespace);
        model.addAttribute("set", set);

        List<AerospikeRecord> records = queryService.getRecords(connectionId, namespace, set);
        if (!records.isEmpty()) {
            List<String> binNames = getBinNames(records);
            model.addAttribute("bins", binNames);
        }
        model.addAttribute("records", records);
        return "record";
    }

    @GetMapping("/delete/{connectionId}/{namespace}/{set}/{key}")
    public String deleteDo(@PathVariable("connectionId") Long connectionId,
                           @PathVariable("namespace") String namespace,
                           @PathVariable("set") String set,
                           @PathVariable("key") String key) {
        queryService.deleteRecord(connectionId, namespace, set, key);
        return "redirect:/record/" + connectionId + "/" + namespace + "/" + set;
    }

    @GetMapping("/query/{connectionId}/{namespace}/{set}")
    public String getQueryPage(@PathVariable("connectionId") Long connectionId,
                               @PathVariable("namespace") String namespace,
                               @PathVariable("set") String set,
                               Model model) {
        model.addAttribute("connectionId", connectionId);
        model.addAttribute("namespace", namespace);
        model.addAttribute("set", set);
        model.addAttribute("query", new QueryDto(null, null, QueryOperator.EQUALS, IndexCollectionType.DEFAULT, null));
        return "query";
    }

    @PostMapping("/query/{connectionId}/{namespace}/{set}")
    public String queryDo(@PathVariable("connectionId") Long connectionId,
                          @PathVariable("namespace") String namespace,
                          @PathVariable("set") String set,
                          @ModelAttribute QueryDto queryDto,
                          Model model) {
        List<AerospikeRecord> records = queryService.query(connectionId, namespace, set, queryDto);
        if (!records.isEmpty()) {
            List<String> binNames = getBinNames(records);
            model.addAttribute("bins", binNames);
        }
        model.addAttribute("records", records);
        return "record";
    }


    @GetMapping("/edit/{connectionId}/{namespace}/{set}/{key}")
    public String getEditRecordPage(@PathVariable("connectionId") Long connectionId,
                                    @PathVariable("namespace") String namespace,
                                    @PathVariable("set") String set,
                                    @PathVariable("key") String key,
                                    Model model) {
        model.addAttribute("connectionId", connectionId);
        model.addAttribute("namespace", namespace);
        model.addAttribute("set", set);
        model.addAttribute("key", key);
        AerospikeRecord record = queryService.getRecord(connectionId, namespace, set, key);
        model.addAttribute("record", new UpdateRecordDto(
            null,
            record.getGeneration(),
            true,
            RecordExistsAction.UPDATE_ONLY,
            GenerationPolicy.NONE,
            record.getRaw()));
        return "editRecord";
    }

    @PostMapping("/edit/{connectionId}/{namespace}/{set}/{key}")
    public String editRecordDo(@PathVariable("connectionId") Long connectionId,
                               @PathVariable("namespace") String namespace,
                               @PathVariable("set") String set,
                               @PathVariable("key") String key,
                               @ModelAttribute UpdateRecordDto updateRecordDto) {
        queryService.updateRecord(connectionId, namespace, set, key, updateRecordDto);
        return "redirect:/record/" + connectionId + "/" + namespace + "/" + set;
    }

    @GetMapping("/create/{connectionId}/{namespace}/{set}")
    public String getCreateRecordPage(@PathVariable("connectionId") Long connectionId,
                                      @PathVariable("namespace") String namespace,
                                      @PathVariable("set") String set,
                                      Model model) {
        model.addAttribute("connectionId", connectionId);
        model.addAttribute("namespace", namespace);
        model.addAttribute("set", set);
        model.addAttribute("record", new CreateRecordDto(null, -1, true, RecordExistsAction.CREATE_ONLY, GenerationPolicy.NONE, null));
        return "createRecord";
    }

    @PostMapping("/create/{connectionId}/{namespace}/{set}")
    public String createRecordDo(@PathVariable("connectionId") Long connectionId,
                                 @PathVariable("namespace") String namespace,
                                 @PathVariable("set") String set,
                                 @ModelAttribute CreateRecordDto createRecordDto) {
        queryService.createRecord(connectionId, namespace, set, createRecordDto);
        return "redirect:/record/" + connectionId + "/" + namespace + "/" + set;
    }

    private List<String> getBinNames(List<AerospikeRecord> records) {
        return records.stream()
            .filter(Objects::nonNull)
            .flatMap(record -> record.getContent().entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null)
                .map(Map.Entry::getKey))
            .distinct()
            .collect(Collectors.toList());
    }
}
