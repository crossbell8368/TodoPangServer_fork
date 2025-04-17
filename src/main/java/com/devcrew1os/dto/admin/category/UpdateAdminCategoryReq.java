package com.devcrew1os.dto.admin.category;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAdminCategoryReq {
    List<UpdateAdminCategoryData> updatedCategories;
}
