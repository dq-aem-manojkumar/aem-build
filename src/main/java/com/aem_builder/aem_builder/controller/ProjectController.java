package com.aem_builder.aem_builder.controller;

import com.aem_builder.aem_builder.model.ProjectRequest;
import com.aem_builder.aem_builder.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/create")
    public String createProjectPage(Model model) {
        model.addAttribute("components", projectService.getAvailableComponents());
        model.addAttribute("projects", projectService.getExistingProjects());
        return "create";
    }

    @PostMapping("/create")
    public String createProject(@ModelAttribute ProjectRequest request, 
                               RedirectAttributes redirectAttributes) {
        try {
            String result = projectService.generateProject(request);
            redirectAttributes.addFlashAttribute("success", "Project created successfully!");
            redirectAttributes.addFlashAttribute("projectName", request.getProjectName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create project: " + e.getMessage());
        }
        return "redirect:/project/create";
    }

    @GetMapping("/import")
    public String importProjectPage(Model model) {
        model.addAttribute("projects", projectService.getExistingProjects());
        return "import";
    }

    @PostMapping("/import")
    public String importProject(@RequestParam("file") MultipartFile file,
                               RedirectAttributes redirectAttributes) {
        try {
            String result = projectService.importProject(file);
            redirectAttributes.addFlashAttribute("success", "Project imported successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to import project: " + e.getMessage());
        }
        return "redirect:/project/import";
    }

    @GetMapping("/details/{projectName}")
    public String projectDetails(@PathVariable String projectName, Model model) {
        try {
            Map<String, Object> projectInfo = projectService.getProjectDetails(projectName);
            model.addAttribute("project", projectInfo);
            model.addAttribute("availableComponents", projectService.getAvailableComponents());
            model.addAttribute("projects", projectService.getExistingProjects());
            return "project-details";
        } catch (Exception e) {
            model.addAttribute("error", "Project not found: " + e.getMessage());
            return "redirect:/";
        }
    }

    @PostMapping("/add-components/{projectName}")
    @ResponseBody
    public ResponseEntity<String> addComponents(@PathVariable String projectName,
                                              @RequestBody Map<String, Object> request) {
        try {
            projectService.addComponentsToProject(projectName, (java.util.List<String>) request.get("components"));
            return ResponseEntity.ok("Components added successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add components: " + e.getMessage());
        }
    }
}