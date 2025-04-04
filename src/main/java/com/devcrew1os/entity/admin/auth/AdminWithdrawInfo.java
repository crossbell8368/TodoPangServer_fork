package com.devcrew1os.entity.admin.auth;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "user_withdraw_info", schema = "admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminWithdrawInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_withdraw_info_id")
    private int id;

    @Column(name = "user_withdraw_reason", nullable = false, length = 255)
    private String reason;

    @Column(name = "user_withdraw_count", nullable = false, length = 255)
    private int count;
}
