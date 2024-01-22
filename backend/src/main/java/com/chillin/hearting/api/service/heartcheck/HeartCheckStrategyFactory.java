package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.MessageService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HeartCheckStrategyFactory {

    private final HeartService heartService;
    private final MessageService messageService;

    public HeartCheckStrategy createHeartCheckStrategy(long heartId) {
        switch ((int) heartId) {
            case 6:
                return new PlanetHeartCheckStrategy();
            case 7:
                return new RainbowHeartCheckStrategy(heartService);
            case 8:
                return new MinchoHeartCheckStrategy(heartService);
            case 9:
                return new SunnyHeartCheckStrategy(heartService);
            case 10:
                return new ReadingGlassesHeartCheckStrategy(heartService, messageService);
            case 11:
                return new IceCreamHeartCheckStrategy(heartService);
            case 12:
                return new ShamrockHeartCheckStrategy(heartService);
            case 13:
                return new FourLeafHeartCheckStrategy(heartService);
            case 14:
                return new NoirHeartCheckStrategy(heartService);
            case 15:
                return new CarnationHeartCheckStrategy();

        }

        return null;
    }
}
