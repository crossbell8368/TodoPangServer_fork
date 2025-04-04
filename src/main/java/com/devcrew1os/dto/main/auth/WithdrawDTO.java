package com.devcrew1os.dto.main.auth;

import com.devcrew1os.entity.admin.auth.AdminWithdrawStat;
import com.devcrew1os.entity.main.user.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WithdrawDTO {
    private UserInfo user;
    private AdminWithdrawStat status;
}
