package com.chillin.hearting.db.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;

@Slf4j
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Heart implements Serializable {

    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment
    private Long id;

    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;

    @Column(name = "image_url", unique = true, nullable = false, length = 200)
    private String imageUrl;

    @Column(name = "short_description", unique = true, nullable = false, length = 500)
    private String shortDescription;

    @Column(name = "long_description", unique = true, nullable = false, length = 500)
    private String longDescription;

    @Column(name = "type", nullable = false, length = 100)
    private String type;

    @Column(name = "acq_condition", unique = true, nullable = true, length = 500)
    private String acqCondition;

    @Builder
    public Heart(Long id, String name, String imageUrl, String shortDescription, String longDescription, String type, String acqCondition) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.shortDescription = shortDescription;
        this.longDescription = longDescription;
        this.type = type;
        this.acqCondition = acqCondition;
    }

}
