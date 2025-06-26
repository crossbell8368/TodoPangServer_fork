package com.devcrew1os.dto.main.home;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class HomeUserData {
    private String userName;
    private int serviceUsedDays;
    private int completeProjects;
}
