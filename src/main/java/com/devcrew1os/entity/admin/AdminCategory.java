package com.devcrew1os.entity.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "challenge_category_info", schema = "admin")
@Getter
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
