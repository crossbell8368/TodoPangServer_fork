package com.devcrew1os.dto.admin.review;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeployAdminReviewReq {
    List<DeployAdminReviewData> data = new ArrayList<>();
}
