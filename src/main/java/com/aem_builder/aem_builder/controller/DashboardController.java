package com.aem_builder.aem_builder.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("pageTitle", "AEM Builder Dashboard");
        return "dashboard";
    }

    @GetMapping("/projects")
    public String projects(Model model) {
        model.addAttribute("pageTitle", "Projects");
        return "projects";
    }

    @GetMapping("/templates")
    public String templates(Model model) {
        model.addAttribute("pageTitle", "Templates");
        return "templates";
    }

    @GetMapping("/components")
    public String components(Model model) {
        model.addAttribute("pageTitle", "Components");
        return "components";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("pageTitle", "Settings");
        return "settings";
    }
}