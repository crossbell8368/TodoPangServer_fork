package com.devcrew1os.dto.main.mypage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetWithdrawReasonData {
    private int reasonId;
    private String reasonDesc;
}
