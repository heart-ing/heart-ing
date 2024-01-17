package com.chillin.hearting.api.service.heartcheck;

import com.chillin.hearting.db.repository.HeartRepository;
import com.chillin.hearting.db.repository.MessageRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class HeartCheckStrategyFactory {

    private final HeartRepository heartRepository;
    private final MessageRepository messageRepository;

    public HeartCheckStrategy createHeartCheckStrategy(long heartId) {
        switch ((int) heartId) {
            case 6:
                return new PlanetHeartCheckStrategy();
            case 7:
                return new RainbowHeartCheckStrategy(heartRepository);
            case 8:
                return new MinchoHeartCheckStrategy(heartRepository);
            case 9:
                return new SunnyHeartCheckStrategy(heartRepository);
            case 10:
                return new ReadingGlassesHeartCheckStrategy(heartRepository, messageRepository);
            case 11:
                return new IceCreamHeartCheckStrategy(heartRepository);
            case 12:
                return new ShamrockHeartCheckStrategy(heartRepository);
            case 13:
                return new FourLeafHeartCheckStrategy(heartRepository);
            case 14:
                return new NoirHeartCheckStrategy(heartRepository);
            case 15:
                return new CarnationHeartCheckStrategy();

        }

        return null;
    }
}
