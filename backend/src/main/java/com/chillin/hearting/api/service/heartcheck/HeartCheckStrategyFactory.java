package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.api.service.HeartService;
import com.chillin.hearting.api.service.MessageService;
import com.chillin.hearting.exception.NoHeartStrategyException;
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
                return new RainbowHeartCheckStrategy(heartService, messageService);
            case 8:
                return new MinchoHeartCheckStrategy(heartService, messageService);
            case 9:
                return new SunnyHeartCheckStrategy(heartService, messageService);
            case 10:
                return new ReadingGlassesHeartCheckStrategy(heartService, messageService);
            case 11:
                return new IceCreamHeartCheckStrategy(heartService, messageService);
            case 12:
                return new ShamrockHeartCheckStrategy(heartService, messageService);
            case 13:
                return new FourLeafHeartCheckStrategy(heartService, messageService);
            case 14:
                return new NoirHeartCheckStrategy(heartService, messageService);
            case 15:
                return new CarnationHeartCheckStrategy();
            default:
                throw new NoHeartStrategyException();

        }
    }
}
