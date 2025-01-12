package com.devcrew1os.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class UserReqDTO {

    /*===========================
        회원가입
    ===========================*/
    @Getter
    @Setter
    public static class Signup {
        private String userId;
        private String name;
        private String email;
        private Integer socialType;
    }

    /*===========================
        로그인
    ===========================*/
    @Getter
    @Setter
    public static class Login {
        private String userId;
    }

}
