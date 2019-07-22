package com.gc.tool.aerospikeclient.controllers;

import java.util.List;

import com.gc.tool.aerospikeclient.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/namespace")
@RequiredArgsConstructor
public class NamespaceController {
    private final QueryService queryService;

    @GetMapping("/{connectionId}")
    public String getQueryPage(@PathVariable("connectionId") Long connectionId, Model model) {
        List<String> nameSpaces = queryService.getNameSpaces(connectionId);
        model.addAttribute("namespaces", nameSpaces);
        model.addAttribute("connectionId", connectionId);
        return "namespace";
    }

}
