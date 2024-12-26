package com.devcrew1os.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.Date;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private String user_id;
    private String user_name;
    private String user_email;
    private Integer user_socialType;
    private boolean user_status;

    private Date user_created_at;
    private Date user_updated_at;
    private Date user_deleted_at;
    private Date user_last_login_at;
    private Date user_last_logout_at;
}
