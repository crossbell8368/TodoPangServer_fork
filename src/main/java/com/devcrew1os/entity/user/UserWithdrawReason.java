package com.devcrew1os.entity.user;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "users_withdraw_reason")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserWithdrawReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reason_id")
    private int id;

    @Column(name = "reason_desc", nullable = false, length = 100)
    private String desc;

    @Column(name = "reason_status", nullable = false)
    private int status;

    @Column(name = "reason_selected_count", nullable = false)
    private int selectedCount;
}
