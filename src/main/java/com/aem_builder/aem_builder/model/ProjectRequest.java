package com.aem_builder.aem_builder.model;

import lombok.Data;
import java.util.List;

@Data
public class ProjectRequest {
    private String projectName;
    private String projectVersion;
    private String packageName;
    private List<String> selectedComponents;
}