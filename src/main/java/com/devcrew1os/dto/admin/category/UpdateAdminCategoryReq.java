package com.devcrew1os.dto.admin.category;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminCategoryReq {
    List<UpdateAdminCategoryData> updatedCategories;
}
