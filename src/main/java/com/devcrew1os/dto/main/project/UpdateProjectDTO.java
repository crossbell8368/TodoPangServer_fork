package com.devcrew1os.dto.main.project;

import com.devcrew1os.entity.project.ProjectTodo;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UpdateProjectDTO {
    private int projectId;
    private List<Integer> challengeIdList;
    private List<Integer> todoIdList;
    private List<ProjectTodo> entityList;
}
