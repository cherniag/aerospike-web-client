package com.gc.tool.aerospikeclient.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gc.tool.aerospikeclient.repository.ConnectionRepository;
import com.gc.tool.aerospikeclient.entities.Connection;

@Controller
@RequestMapping("/connection")
@Slf4j
@RequiredArgsConstructor
public class ConnectionController {
    private final ConnectionRepository connectionRepository;

    @GetMapping
    public String connections(Model model) {
        Iterable<Connection> connections = connectionRepository.findAll();
        model.addAttribute("connections", connections);
        return "connection";
    }

    @GetMapping("/create")
    public String addConnectionPage(Model model) {
        model.addAttribute("connection", new Connection());
        return "createConnection";
    }

    @PostMapping("/create")
    public String addConnectionDo(@ModelAttribute Connection connection, Model model) {
        log.info("Create connection {}", connection);
        connectionRepository.save(connection);

        Iterable<Connection> connections = connectionRepository.findAll();
        model.addAttribute("connections", connections);
        return "redirect:/connection";
    }

    @GetMapping("/delete/{id}")
    public String deleteConnectionDo(@PathVariable("id") long id, Model model) {
        log.info("Delete connection id {}", id);
        connectionRepository.deleteById(id);

        Iterable<Connection> connections = connectionRepository.findAll();
        model.addAttribute("connections", connections);
        return "redirect:/connection";
    }

}
