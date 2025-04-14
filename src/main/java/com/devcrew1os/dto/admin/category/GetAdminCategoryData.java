package com.devcrew1os.dto.admin.category;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAdminCategoryData {
    private int categoryId;
    private int categoryStatus;
    private String categoryTitle;
    private int challengesInvolved;
    private String lastUpdatedBy;
    private LocalDateTime lastUpdatedAt;
}
