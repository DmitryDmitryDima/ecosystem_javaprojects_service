package com.ecosystem.projectsservice.javaprojects.dto.dashboard;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AvatarsWithIndexes {

    private List<AvatarDTO> avatars;

    private List<IndexGroupDTO> indexes;
}
