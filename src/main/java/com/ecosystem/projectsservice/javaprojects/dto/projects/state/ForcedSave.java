package com.ecosystem.projectsservice.javaprojects.dto.projects.state;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ForcedSave {

    private FileDTO fileDTO;

}
