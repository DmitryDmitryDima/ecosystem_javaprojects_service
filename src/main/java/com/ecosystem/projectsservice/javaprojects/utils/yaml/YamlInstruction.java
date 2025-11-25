package com.ecosystem.projectsservice.javaprojects.utils.yaml;

// класс для репрезентации инструкции yaml

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// класс для репрезентации yaml инструкции
@Data
@AllArgsConstructor
@NoArgsConstructor
public class YamlInstruction {
    private List<DirectoryInstruction> directories;
    private List<FileInstruction> files;
}
