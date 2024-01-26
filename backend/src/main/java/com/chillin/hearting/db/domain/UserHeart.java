package com.chillin.hearting.db.domain;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Builder
public class UserHeart implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "heart_id", nullable = false)
    private Heart heart;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    public static UserHeart of(User user, Heart heart) {
        return UserHeart.builder()
                .user(user)
                .heart(heart)
                .createdDate(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();
    }
}
