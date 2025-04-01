package com.devcrew1os.dto.admin.category;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateAdminCategoryReq {
    private String adminId;
    List<UpdateAdminCategoryData> updatedCategories;
}
