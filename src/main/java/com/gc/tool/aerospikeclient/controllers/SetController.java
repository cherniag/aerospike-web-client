package com.gc.tool.aerospikeclient.controllers;

import java.util.List;

import com.gc.tool.aerospikeclient.service.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gc.tool.aerospikeclient.dto.AerospikeSet;

@Controller
@RequestMapping("/set")
@Slf4j
@RequiredArgsConstructor
public class SetController {
    private final QueryService queryService;

    @GetMapping("/{connectionId}/{namespace}")
    public String getSetPage(@PathVariable("connectionId") Long connectionId,
                             @PathVariable("namespace") String namespace,
                             Model model) {
        List<AerospikeSet> sets = queryService.getSets(connectionId, namespace);
        model.addAttribute("sets", sets);
        model.addAttribute("connectionId", connectionId);
        return "set";
    }
}
