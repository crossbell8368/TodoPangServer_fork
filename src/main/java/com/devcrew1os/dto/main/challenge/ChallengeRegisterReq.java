package com.devcrew1os.dto.main.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeRegisterReq {
    private Integer challengeId;
    private List<Integer> todoIds;
}
