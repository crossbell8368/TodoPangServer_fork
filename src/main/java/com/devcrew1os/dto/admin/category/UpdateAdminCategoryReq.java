package com.devcrew1os.dto.admin.category;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class UpdateAdminCategoryReq {
    private String adminId;
    private Integer updatedCategoryId;
    private String updatedCategoryTitle;
}
