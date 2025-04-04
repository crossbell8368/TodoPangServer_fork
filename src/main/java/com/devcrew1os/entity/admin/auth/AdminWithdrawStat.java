package com.devcrew1os.entity.admin.auth;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_withdraw_stat", schema = "admin")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminWithdrawStat {

    @Id
    @Column(name = "user_withdraw_stat_id")
    private String userId;

    @ManyToOne()
    @JoinColumn(name = "user_withdraw_info_id", referencedColumnName = "user_withdraw_info_id")
    private AdminWithdrawInfo userWithdrawInfo;

    @Column(name = "user_withdraw_at")
    private LocalDateTime userWithdrawAt;
}
