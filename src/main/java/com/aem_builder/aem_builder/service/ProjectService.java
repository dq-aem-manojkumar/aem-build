package com.aem_builder.aem_builder.service;

import com.aem_builder.aem_builder.model.ProjectRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ProjectService {

    private static final String COMPONENTS_PATH = "src/main/resources/components";
    private static final String GENERATED_PROJECTS_PATH = "generated-projects";

    public List<String> getAvailableComponents() {
        try {
            Path componentsDir = Paths.get(COMPONENTS_PATH);
            if (!Files.exists(componentsDir)) {
                Files.createDirectories(componentsDir);
                // Create some sample components
                createSampleComponents();
            }
            
            return Files.list(componentsDir)
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return Arrays.asList("header", "footer", "navigation", "carousel", "text", "image");
        }
    }

    public List<Map<String, Object>> getExistingProjects() {
        try {
            Path projectsDir = Paths.get(GENERATED_PROJECTS_PATH);
            if (!Files.exists(projectsDir)) {
                Files.createDirectories(projectsDir);
                return new ArrayList<>();
            }

            return Files.list(projectsDir)
                    .filter(Files::isDirectory)
                    .map(this::getProjectInfo)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public String generateProject(ProjectRequest request) throws Exception {
        // Create project directory
        Path projectDir = Paths.get(GENERATED_PROJECTS_PATH, request.getProjectName());
        Files.createDirectories(projectDir);

        // Create project structure
        createProjectStructure(projectDir, request);
        
        // Save project metadata
        saveProjectMetadata(projectDir, request);

        return "Project generated successfully at: " + projectDir.toString();
    }

    public String importProject(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Invalid file name");
        }

        String projectName = originalFilename.replaceAll("\\.(zip|jar)$", "");
        Path projectDir = Paths.get(GENERATED_PROJECTS_PATH, projectName);
        Files.createDirectories(projectDir);

        // Extract the uploaded file
        if (originalFilename.endsWith(".zip") || originalFilename.endsWith(".jar")) {
            extractZipFile(file.getInputStream(), projectDir);
        } else {
            throw new IllegalArgumentException("Only .zip and .jar files are supported");
        }

        // Create basic metadata for imported project
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", projectName);
        metadata.put("version", "imported");
        metadata.put("components", Arrays.asList("imported-components"));
        metadata.put("imported", true);
        
        saveMetadata(projectDir, metadata);

        return "Project imported successfully";
    }

    public Map<String, Object> getProjectDetails(String projectName) throws Exception {
        Path projectDir = Paths.get(GENERATED_PROJECTS_PATH, projectName);
        if (!Files.exists(projectDir)) {
            throw new FileNotFoundException("Project not found: " + projectName);
        }

        return loadMetadata(projectDir);
    }

    public void addComponentsToProject(String projectName, List<String> components) throws Exception {
        Path projectDir = Paths.get(GENERATED_PROJECTS_PATH, projectName);
        if (!Files.exists(projectDir)) {
            throw new FileNotFoundException("Project not found: " + projectName);
        }

        Map<String, Object> metadata = loadMetadata(projectDir);
        List<String> existingComponents = (List<String>) metadata.getOrDefault("components", new ArrayList<>());
        
        for (String component : components) {
            if (!existingComponents.contains(component)) {
                existingComponents.add(component);
                // Copy component files to project
                copyComponentToProject(projectDir, component);
            }
        }

        metadata.put("components", existingComponents);
        saveMetadata(projectDir, metadata);
    }

    private void createSampleComponents() throws IOException {
        String[] components = {"header", "footer", "navigation", "carousel", "text", "image", "form", "gallery"};
        
        for (String component : components) {
            Path componentDir = Paths.get(COMPONENTS_PATH, component);
            Files.createDirectories(componentDir);
            
            // Create sample component files
            Files.write(componentDir.resolve(component + ".html"), 
                String.format("<!-- %s component template -->%n<div class=\"%s-component\">%n  <!-- Component content -->%n</div>", 
                    component, component).getBytes());
            
            Files.write(componentDir.resolve(component + ".js"), 
                String.format("// %s component JavaScript%nconsole.log('%s component loaded');", 
                    component, component).getBytes());
        }
    }

    private Map<String, Object> getProjectInfo(Path projectDir) {
        Map<String, Object> info = new HashMap<>();
        String projectName = projectDir.getFileName().toString();
        info.put("name", projectName);
        
        try {
            Map<String, Object> metadata = loadMetadata(projectDir);
            info.putAll(metadata);
        } catch (Exception e) {
            info.put("version", "unknown");
            info.put("components", new ArrayList<>());
        }
        
        return info;
    }

    private void createProjectStructure(Path projectDir, ProjectRequest request) throws IOException {
        // Create basic AEM project structure
        Files.createDirectories(projectDir.resolve("src/main/content/jcr_root/apps/" + request.getPackageName()));
        Files.createDirectories(projectDir.resolve("src/main/content/jcr_root/etc/designs/" + request.getPackageName()));
        Files.createDirectories(projectDir.resolve("src/main/java/" + request.getPackageName().replace(".", "/")));
        
        // Create pom.xml
        String pomContent = generatePomXml(request);
        Files.write(projectDir.resolve("pom.xml"), pomContent.getBytes());
        
        // Copy selected components
        if (request.getSelectedComponents() != null) {
            for (String component : request.getSelectedComponents()) {
                copyComponentToProject(projectDir, component);
            }
        }
    }

    private void copyComponentToProject(Path projectDir, String componentName) throws IOException {
        Path sourceComponent = Paths.get(COMPONENTS_PATH, componentName);
        Path targetComponent = projectDir.resolve("components/" + componentName);
        
        if (Files.exists(sourceComponent)) {
            Files.createDirectories(targetComponent.getParent());
            copyDirectory(sourceComponent, targetComponent);
        }
    }

    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(sourcePath -> {
            try {
                Path targetPath = target.resolve(source.relativize(sourcePath));
                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String generatePomXml(ProjectRequest request) {
        return String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0"
                     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                     xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
                     http://maven.apache.org/xsd/maven-4.0.0.xsd">
                <modelVersion>4.0.0</modelVersion>
                
                <groupId>%s</groupId>
                <artifactId>%s</artifactId>
                <version>%s</version>
                <packaging>content-package</packaging>
                
                <name>%s</name>
                <description>AEM project generated by AEM Builder</description>
                
                <properties>
                    <maven.compiler.source>11</maven.compiler.source>
                    <maven.compiler.target>11</maven.compiler.target>
                    <aem.version>6.5.0</aem.version>
                </properties>
                
                <dependencies>
                    <dependency>
                        <groupId>com.adobe.aem</groupId>
                        <artifactId>uber-jar</artifactId>
                        <version>${aem.version}</version>
                        <scope>provided</scope>
                    </dependency>
                </dependencies>
            </project>
            """, request.getPackageName(), request.getProjectName(), 
                 request.getProjectVersion(), request.getProjectName());
    }

    private void saveProjectMetadata(Path projectDir, ProjectRequest request) throws IOException {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("name", request.getProjectName());
        metadata.put("version", request.getProjectVersion());
        metadata.put("packageName", request.getPackageName());
        metadata.put("components", request.getSelectedComponents() != null ? request.getSelectedComponents() : new ArrayList<>());
        metadata.put("createdAt", System.currentTimeMillis());
        
        saveMetadata(projectDir, metadata);
    }

    private void saveMetadata(Path projectDir, Map<String, Object> metadata) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue().toString()).append("\n");
        }
        Files.write(projectDir.resolve(".metadata"), sb.toString().getBytes());
    }

    private Map<String, Object> loadMetadata(Path projectDir) throws IOException {
        Path metadataFile = projectDir.resolve(".metadata");
        Map<String, Object> metadata = new HashMap<>();
        
        if (Files.exists(metadataFile)) {
            List<String> lines = Files.readAllLines(metadataFile);
            for (String line : lines) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0];
                    String value = parts[1];
                    
                    if (key.equals("components")) {
                        // Parse components list
                        if (value.startsWith("[") && value.endsWith("]")) {
                            value = value.substring(1, value.length() - 1);
                            metadata.put(key, Arrays.asList(value.split(",\\s*")));
                        } else {
                            metadata.put(key, Arrays.asList(value));
                        }
                    } else {
                        metadata.put(key, value);
                    }
                }
            }
        }
        
        return metadata;
    }

    private void extractZipFile(InputStream inputStream, Path targetDir) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(inputStream)) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                Path filePath = targetDir.resolve(entry.getName());
                if (!entry.isDirectory()) {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zipIn, filePath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.createDirectories(filePath);
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        }
    }
}