package com.chillin.hearting.api.data;

import com.chillin.hearting.db.domain.Heart;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class HeartConditionData implements Data {
    private Long heartId;
    private String name;
    private String heartUrl;
    private int currentValue;
    private int maxValue;

    public static HeartConditionData of(Heart heart) {
        return HeartConditionData.builder()
                .heartId((heart != null) ? heart.getId() : null)
                .name((heart != null) ? heart.getName() : null)
                .heartUrl((heart != null) ? heart.getImageUrl() : null)
                .build();
    }

    public static HeartConditionData of(Heart heart, int currentValue, int maxValue) {
        return HeartConditionData.builder()
                .heartId((heart != null) ? heart.getId() : null)
                .name((heart != null) ? heart.getName() : null)
                .heartUrl((heart != null) ? heart.getImageUrl() : null)
                .currentValue(currentValue)
                .maxValue(maxValue)
                .build();
    }
}
