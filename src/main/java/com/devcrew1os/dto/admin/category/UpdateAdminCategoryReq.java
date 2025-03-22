package com.devcrew1os.dto.admin.category;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAdminCategoryReq {
    private String adminId;
    private Integer updatedCategoryId;
    private Integer updatedCategoryStatus;
    private String updatedCategoryTitle;
}
