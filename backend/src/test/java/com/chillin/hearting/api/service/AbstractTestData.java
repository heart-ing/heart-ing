package com.chillin.hearting.api.service;

import com.chillin.hearting.api.service.enums.HeartType;
import com.chillin.hearting.db.domain.*;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ScanOptions;

import java.time.LocalDateTime;
import java.util.*;
public class AbstractTestData {

    protected Heart like;
    protected Heart cheer;
    protected Heart friendship;
    protected Heart flutter;
    protected Heart love;
    protected Heart planet;
    protected Heart rainbow;
    protected Heart mincho;
    protected Heart sunny;
    protected Heart readingGlasses;
    protected Heart iceCream;
    protected Heart shamrock;
    protected Heart fourLeaf;
    protected Heart noir;
    protected Heart carnation;
    protected List<Heart> heartList = new ArrayList<>();
    protected List<Heart> defaultHeartList = new ArrayList<>();
    protected List<Heart> specialHeartList = new ArrayList<>();
    protected List<Heart> eventHeartList = new ArrayList<>();

    protected User sender;
    protected User receiver;

    protected Message message;

    protected Emoji likeEmoji;
    protected Emoji zzEmoji;
    protected Emoji bestEmoji;
    protected Emoji sadEmoji;
    protected Emoji checkEmoji;

    protected Long notExistHeartId;
    protected Long existHeartId;

    public AbstractTestData() {
        // Default Type
        like = createHeart(1L, "호감", HeartType.DEFAULT.name());
        cheer = createHeart(2L, "응원", HeartType.DEFAULT.name());
        friendship = createHeart(3L, "우정", HeartType.DEFAULT.name());
        flutter = createHeart(4L, "애정", HeartType.DEFAULT.name());
        love = createHeart(5L, "사랑", HeartType.DEFAULT.name());

        // Event Type
        planet = createHeart(6L, "행성", HeartType.EVENT.name());

        // Special Type
        rainbow = createHeart(7L, "무지개", HeartType.SPECIAL.name());
        mincho = createHeart(8L, "민초", HeartType.SPECIAL.name());
        sunny = createHeart(9L, "햇살", HeartType.SPECIAL.name());
        readingGlasses = createHeart(10L, "돋보기", HeartType.SPECIAL.name());
        iceCream = createHeart(11L, "아이스크림", HeartType.SPECIAL.name());
        shamrock = createHeart(12L, "세잎클로버", HeartType.SPECIAL.name());
        fourLeaf = createHeart(13L, "네잎클로버", HeartType.SPECIAL.name());
        noir = createHeart(14L, "질투의 누아르", HeartType.SPECIAL.name());

        // Event Type
        carnation = createHeart(15L, "카네이션", HeartType.EVENT.name());

        heartList.add(like);
        heartList.add(cheer);
        heartList.add(friendship);
        heartList.add(flutter);
        heartList.add(love);
        heartList.add(planet);
        heartList.add(rainbow);
        heartList.add(mincho);
        heartList.add(sunny);
        heartList.add(readingGlasses);
        heartList.add(iceCream);
        heartList.add(shamrock);
        heartList.add(fourLeaf);
        heartList.add(noir);
        heartList.add(carnation);

        defaultHeartList.add(like);
        defaultHeartList.add(cheer);
        defaultHeartList.add(friendship);
        defaultHeartList.add(flutter);
        defaultHeartList.add(love);

        specialHeartList.add(rainbow);
        specialHeartList.add(mincho);
        specialHeartList.add(sunny);
        specialHeartList.add(readingGlasses);
        specialHeartList.add(iceCream);
        specialHeartList.add(shamrock);
        specialHeartList.add(fourLeaf);
        specialHeartList.add(noir);

        eventHeartList.add(planet);
        eventHeartList.add(carnation);

        sender = createUser("sender");
        receiver = createUser("receiver");

        likeEmoji = createEmoji(1L,"like");
        zzEmoji = createEmoji(2L, "zz");
        bestEmoji = createEmoji(3L, "best");
        sadEmoji = createEmoji(4L, "sad");
        checkEmoji = createEmoji(5L, "check");

        message = createMessage(1L,like,sender,receiver,likeEmoji);
    }

    protected Emoji createEmoji(long id, String name) {
        return Emoji.builder().id(id).name(name).imageUrl(name).build();
    }

    protected Emoji createEmoji(String name) {
        return Emoji.builder().name(name).imageUrl(name).build();
    }

    protected Heart createHeart(long id, String name, String type) {
        Heart heart = Heart.builder()
                .id(id)
                .name(name)
                .imageUrl(name)
                .shortDescription("짧은 설명 !")
                .longDescription("호감의 탄생 스토리")
                .acqCondition("기본 제공")
                .type(type)
                .build();

        return heart;
    }

    protected Heart createHeart(String name, String type) {
        Heart heart = Heart.builder()
                .name(name)
                .imageUrl(name)
                .shortDescription("짧은 설명 !")
                .longDescription("호감의 탄생 스토리")
                .acqCondition("기본 제공")
                .type(type)
                .build();

        return heart;
    }

    protected User createUser(String id) {
        User user = User.builder()
                .id(id)
                .type(id)
                .email(id)
                .nickname(id)
                .messageTotal(0L)
                .build();
        return user;
    }

    protected Message createMessage(Long id, Heart heart, User sender, User receiver, Emoji emoji) {
        return Message.builder()
                .id(id)
                .title("title")
                .heart(heart)
                .sender(sender)
                .receiver(receiver)
                .emoji(emoji)
                .build();
    }

    protected Notification createNotification(User user, Message message, Heart heart, LocalDateTime dateTime) {
        return Notification.builder()
                .user(user)
                .message(message)
                .heart(heart)
                .type("type")
                .expiredDate(dateTime)
                .build();
    }

    protected HashOperations<String, String, Object> getMockRedisHashOperations() {
        return new HashOperations<String, String, Object>() {
            @Override
            public Long delete(String key, Object... hashKeys) {
                return null;
            }

            @Override
            public Boolean hasKey(String key, Object hashKey) {
                return null;
            }

            @Override
            public Object get(String key, Object hashKey) {
                if (Long.parseLong(hashKey.toString()) == notExistHeartId) return null;
                else return 1;
            }

            @Override
            public List<Object> multiGet(String key, Collection<String> hashKeys) {
                return null;
            }

            @Override
            public Long increment(String key, String hashKey, long delta) {
                return null;
            }

            @Override
            public Double increment(String key, String hashKey, double delta) {
                return null;
            }

            @Override
            public String randomKey(String key) {
                return null;
            }

            @Override
            public Map.Entry<String, Object> randomEntry(String key) {
                return null;
            }

            @Override
            public List<String> randomKeys(String key, long count) {
                return null;
            }

            @Override
            public Map<String, Object> randomEntries(String key, long count) {
                return null;
            }

            @Override
            public Set<String> keys(String key) {
                return null;
            }

            @Override
            public Long lengthOfValue(String key, String hashKey) {
                return null;
            }

            @Override
            public Long size(String key) {
                return null;
            }

            @Override
            public void putAll(String key, Map<? extends String, ?> m) {

            }

            @Override
            public void put(String key, String hashKey, Object value) {

            }

            @Override
            public Boolean putIfAbsent(String key, String hashKey, Object value) {
                return null;
            }

            @Override
            public List<Object> values(String key) {
                return null;
            }

            @Override
            public Map<String, Object> entries(String key) {
                return null;
            }

            @Override
            public Cursor<Map.Entry<String, Object>> scan(String key, ScanOptions options) {
                return null;
            }

            @Override
            public RedisOperations<String, ?> getOperations() {
                return null;
            }
        };
    }
}
