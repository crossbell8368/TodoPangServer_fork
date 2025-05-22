package com.devcrew1os.dto.main.mypage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddUserOpinionReq {
    private int satisfiedRating;
    private String comment;
}
