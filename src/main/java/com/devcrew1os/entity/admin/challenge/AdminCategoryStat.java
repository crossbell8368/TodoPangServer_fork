package com.devcrew1os.entity.admin.challenge;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "challenge_category_stat", schema = "admin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminCategoryStat {

    @Id
    @Column(name = "challenge_category_info_id")
    private int id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "challenge_category_info_id")
    private AdminCategoryInfo categoryInfo;

    @Column(name = "challenge_category_involved_count", nullable = false)
    private int involvedCount;
}
