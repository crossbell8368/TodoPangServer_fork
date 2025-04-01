package com.devcrew1os.dto.admin.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminCategoryData {
    private Integer updatedCategoryId;
    private Integer updatedCategoryStatus;
    private String updatedCategoryTitle;
}
