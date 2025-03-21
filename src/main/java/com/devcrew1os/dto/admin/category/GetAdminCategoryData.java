package com.devcrew1os.dto.admin.category;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class GetAdminCategoryData {
    private Map<Integer, String> categories;
}
