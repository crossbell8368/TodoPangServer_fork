package com.devcrew1os.entity.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_withdraw_log", schema = "log")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserWithdrawLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_withdraw_log_id")
    private int id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "user_withdraw_reason")
    private int withdrawReason;

    @Column(name = "user_withdraw_at")
    private LocalDateTime withdrawAt;
}
