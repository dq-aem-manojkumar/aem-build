package com.aem_builder.aem_builder.controller;

import com.aem_builder.aem_builder.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("projects", projectService.getExistingProjects());
        model.addAttribute("components", projectService.getAvailableComponents());
        return "home";
    }
}