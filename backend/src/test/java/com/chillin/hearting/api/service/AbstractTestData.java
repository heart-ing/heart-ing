package com.chillin.hearting.api.service;

import com.chillin.hearting.api.service.enums.HeartType;
import com.chillin.hearting.db.domain.*;

import java.time.LocalDateTime;
import java.util.*;
public class AbstractTestData {

    protected Heart like = createHeart(1L, "호감", HeartType.DEFAULT.name());
    protected Heart cheer = createHeart(2L, "응원", HeartType.DEFAULT.name());
    protected Heart friendship = createHeart(3L, "우정", HeartType.DEFAULT.name());
    protected Heart flutter = createHeart(4L, "애정", HeartType.DEFAULT.name());
    protected Heart love = createHeart(5L, "사랑", HeartType.DEFAULT.name());
    protected Heart planet = createHeart(6L, "행성", HeartType.EVENT.name());
    protected Heart rainbow = createHeart(7L, "무지개", HeartType.SPECIAL.name());
    protected Heart mincho = createHeart(8L, "민초", HeartType.SPECIAL.name());
    protected Heart sunny = createHeart(9L, "햇살", HeartType.SPECIAL.name());
    protected Heart readingGlasses = createHeart(10L, "돋보기", HeartType.SPECIAL.name());
    protected Heart iceCream = createHeart(11L, "아이스크림", HeartType.SPECIAL.name());
    protected Heart shamrock = createHeart(12L, "세잎클로버", HeartType.SPECIAL.name());
    protected Heart fourLeaf = createHeart(13L, "네잎클로버", HeartType.SPECIAL.name());
    protected Heart noir = createHeart(14L, "질투의 누아르", HeartType.SPECIAL.name());
    protected Heart carnation = createHeart(15L, "카네이션", HeartType.EVENT.name());
    protected List<Heart> heartList = List.of(
            like, cheer, friendship, flutter, love,
            planet,
            rainbow, mincho, sunny, readingGlasses, iceCream, shamrock, fourLeaf, noir,
            carnation);
    protected List<Heart> defaultHeartList = List.of(like, cheer, friendship, flutter, love);
    protected List<Heart> specialHeartList = List.of(rainbow, mincho, sunny, readingGlasses, iceCream, shamrock, fourLeaf, noir);
    protected List<Heart> eventHeartList = List.of(planet, carnation);

    protected User sender = createUser("sender");
    protected User receiver = createUser("receiver");


    protected Emoji likeEmoji = createEmoji(1L,"like");
    protected Emoji zzEmoji = createEmoji(2L, "zz");
    protected Emoji bestEmoji = createEmoji(3L, "best");
    protected Emoji sadEmoji = createEmoji(4L, "sad");
    protected Emoji checkEmoji = createEmoji(5L, "check");

    protected Message message = createMessage(1L,like,sender,receiver,likeEmoji);

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
                .isActive(true)
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
}
