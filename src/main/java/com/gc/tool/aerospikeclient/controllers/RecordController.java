package com.gc.tool.aerospikeclient.controllers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gc.tool.aerospikeclient.dto.CreateRecordDto;
import com.gc.tool.aerospikeclient.dto.AerospikeRecord;
import com.gc.tool.aerospikeclient.service.QueryService;

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
            List<String> binNames = records.stream()
                .flatMap(record -> record.getContent().entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() != null)
                    .map(Map.Entry::getKey))
                .distinct()
                .collect(Collectors.toList());
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


    @GetMapping("/edit/{connectionId}/{namespace}/{set}/{key}")
    public String getEditRecordPage(@PathVariable("connectionId") Long connectionId,
                                    @PathVariable("namespace") String namespace,
                                    @PathVariable("set") String set,
                                    @PathVariable("key") String key,
                                    Model model) {
        model.addAttribute("connectionId", connectionId);
        model.addAttribute("namespace", namespace);
        model.addAttribute("set", set);
        AerospikeRecord record = queryService.getRecord(connectionId, namespace, set, key);
        model.addAttribute("record", record);
        return "editRecord";
    }

    @PostMapping("/edit/{connectionId}/{namespace}/{set}/{key}")
    public String editRecordDo(@PathVariable("connectionId") Long connectionId,
                               @PathVariable("namespace") String namespace,
                               @PathVariable("set") String set,
                               @PathVariable("key") String key,
                               @ModelAttribute AerospikeRecord aerospikeRecord) {
        queryService.updateRecord(connectionId, namespace, set, key, aerospikeRecord);
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
        model.addAttribute("record", new CreateRecordDto(null, null, 0, false));
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
}
