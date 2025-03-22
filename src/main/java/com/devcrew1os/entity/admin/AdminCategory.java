package com.devcrew1os.entity.admin;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "challenge_category_info", schema = "admin")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "challenge_category_info_id")
    private int id;

    @Column(name = "challenge_category_title", nullable = false, length = 255)
    private String title;
}
