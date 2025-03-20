package com.devcrew1os.dto.home;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class HomeUserData {
    private String userName;
    private int finishedProjects;
    private int registeredProjects;
}
