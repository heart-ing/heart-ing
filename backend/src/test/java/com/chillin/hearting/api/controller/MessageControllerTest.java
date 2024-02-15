package com.chillin.hearting.api.controller;

import com.chillin.hearting.api.data.EmojiData;
import com.chillin.hearting.api.data.ReportData;
import com.chillin.hearting.api.data.SendMessageData;
import com.chillin.hearting.api.request.ReportReq;
import com.chillin.hearting.api.request.SendMessageReq;
import com.chillin.hearting.api.service.facade.MessageFacade;
import com.chillin.hearting.db.domain.User;
import com.chillin.hearting.exception.*;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @InjectMocks
    private MessageController messageController;
    @Mock
    private MessageFacade messageFacade;
    private MockMvc mockMvc;
    private Gson gson;
    private static final String SUCCESS = "success";
    private final long messageId = 0L;
    private final long heartId = 0L;
    private final long emojiId = 0L;
    private final String senderId = "senderId";
    private final String receiverId = "receiverId";
    private final String content = "content";
    private final String title = "title";

    @BeforeEach
    public void init() {
        gson = new Gson();
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).setControllerAdvice(new ControllerExceptionHandler()).build();
    }

    private SendMessageReq createSendMessageReq() {
        return SendMessageReq.builder().heartId(heartId).senderId(senderId).receiverId(receiverId).title(title).build();
    }

    @Test
    @DisplayName("메시지 전송 성공 - 로그인 상태")
    public void sendMessageLogin() throws Exception {
        // given
        final String url = "/api/v1/messages/";
        User user = User.builder().id(senderId).build();

        SendMessageReq sendMessageReq = createSendMessageReq();

        SendMessageData expectedResponse = SendMessageData.builder()
                .messageId(messageId)
                .heartId(sendMessageReq.getHeartId())
                .build();

        doReturn(expectedResponse)
                .when(messageFacade)
                .sendMessage(
                        sendMessageReq.getHeartId(),
                        sendMessageReq.getSenderId(),
                        sendMessageReq.getReceiverId(),
                        sendMessageReq.getTitle(),
                        sendMessageReq.getContent(),
                        "127.0.0.1");

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .content(gson.toJson(sendMessageReq))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            request.addHeader("X-Forwarded-For","127.0.0.1");
                            return request;
                        })
        );

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.messageId", is((int) messageId)))
                .andExpect(jsonPath("$.data.heartId", is((int) heartId)))
                .andExpect(jsonPath("$.status", is(SUCCESS)));
    }

    @Test
    @DisplayName("메시지 전송 성공 - 로그인, X-FORWARDED-FOR 없음")
    public void sendMessageLoginWithNoXForwardedFor() throws Exception {
        // given
        final String url = "/api/v1/messages/";
        User user = User.builder().id(senderId).build();

        SendMessageReq sendMessageReq = createSendMessageReq();

        SendMessageData expectedResponse = SendMessageData.builder()
                .messageId(messageId)
                .heartId(sendMessageReq.getHeartId())
                .build();

        doReturn(expectedResponse)
                .when(messageFacade)
                .sendMessage(
                        sendMessageReq.getHeartId(),
                        sendMessageReq.getSenderId(),
                        sendMessageReq.getReceiverId(),
                        sendMessageReq.getTitle(),
                        sendMessageReq.getContent(),
                        "127.0.0.1");

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .content(gson.toJson(sendMessageReq))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.messageId", is((int) messageId)))
                .andExpect(jsonPath("$.data.heartId", is((int) heartId)))
                .andExpect(jsonPath("$.status", is(SUCCESS)));
    }

    @Test
    @DisplayName("메시지 전송 성공 - 로그인, X-FORWARDED-FOR empty")
    public void sendMessageLoginWithEmptyXForwardedFor() throws Exception {
        // given
        final String url = "/api/v1/messages/";
        User user = User.builder().id(senderId).build();

        SendMessageReq sendMessageReq = createSendMessageReq();

        SendMessageData expectedResponse = SendMessageData.builder()
                .messageId(messageId)
                .heartId(sendMessageReq.getHeartId())
                .build();

        doReturn(expectedResponse)
                .when(messageFacade)
                .sendMessage(
                        sendMessageReq.getHeartId(),
                        sendMessageReq.getSenderId(),
                        sendMessageReq.getReceiverId(),
                        sendMessageReq.getTitle(),
                        sendMessageReq.getContent(),
                        "127.0.0.1");

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .content(gson.toJson(sendMessageReq))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            request.addHeader("X-Forwarded-For","");
                            return request;
                        })
        );

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.messageId", is((int) messageId)))
                .andExpect(jsonPath("$.data.heartId", is((int) heartId)))
                .andExpect(jsonPath("$.status", is(SUCCESS)));
    }

    @Test
    @DisplayName("메시지 전송 성공 - 비로그인 상태")
    public void sendMessageNoLogin() throws Exception {
        // given
        final String url = "/api/v1/messages/";

        SendMessageReq sendMessageReq = createSendMessageReq();
        sendMessageReq.setSenderId(null);

        SendMessageData expectedResponse = SendMessageData.builder()
                .messageId(messageId)
                .heartId(sendMessageReq.getHeartId())
                .build();

        doReturn(expectedResponse)
                .when(messageFacade)
                .sendMessage(
                        sendMessageReq.getHeartId(),
                        null,
                        sendMessageReq.getReceiverId(),
                        sendMessageReq.getTitle(),
                        sendMessageReq.getContent(),
                        "127.0.0.1");

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .content(gson.toJson(sendMessageReq))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.messageId", is((int) messageId)))
                .andExpect(jsonPath("$.data.heartId", is((int) heartId)))
                .andExpect(jsonPath("$.status", is(SUCCESS)));
    }

    @Test
    @DisplayName("메시지 전송 실패 - 전송자 아이디 다름")
    public void sendMessageFailSenderIdNotUserId() throws Exception {
        // given
        final String url = "/api/v1/messages";
        User user = User.builder().id("otherSender").build();
        SendMessageReq sendMessageReq = createSendMessageReq();

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .content(gson.toJson(sendMessageReq))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is(WrongUserException.DEFAULT_MESSAGE)));
    }

    @Test
    @DisplayName("메시지 전송 실패 - 본인에게 전송")
    public void sendMessageFailSendYourSelf() throws Exception {
        // given
        final String url = "/api/v1/messages";
        User user = User.builder().id(senderId).build();
        SendMessageReq sendMessageReq = createSendMessageReq();
        sendMessageReq.setReceiverId(senderId);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .content(gson.toJson(sendMessageReq))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("본인에게 메시지를 보냈습니다.")));
    }

    @Test
    @DisplayName("메시지 전송 실패 - 메시지 제목 길이 초과")
    public void sendMessageFailTitleLengthTooLong() throws Exception {
        // given
        final String url = "/api/v1/messages";
        User user = User.builder().id(senderId).build();
        SendMessageReq sendMessageReq = createSendMessageReq();
        sendMessageReq.setTitle("aaaabbbbccccd");

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .content(gson.toJson(sendMessageReq))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("제목이 12자를 초과하였습니다.")));
    }

    @Test
    @DisplayName("메시지 삭제 성공")
    public void deleteMessage() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId;
        User user = User.builder().id(senderId).build();

        doReturn(false).when(messageFacade).deleteMessage(messageId, user.getId());

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("메시지가 성공적으로 삭제되었습니다.")))
                .andExpect(jsonPath("$.status", is(SUCCESS)));
    }

    @Test
    @DisplayName("메시지 삭제 실패 - 비로그인 상태")
    public void deleteMessageFailNullUser() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId;

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is(UnAuthorizedException.DEFAULT_MESSAGE)));
    }

    @Test
    @DisplayName("메시지 삭제 실패 - 서버 에러")
    public void deleteMessageFailServerError() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId;
        User user = User.builder().id(receiverId).build();
        doReturn(true).when(messageFacade).deleteMessage(messageId, receiverId);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is(DeleteMessageFailException.DEFAULT_MESSAGE)));
    }

    @Test
    @DisplayName("메시지 삭제 실패 - 본인에게 오지 않은 메시지 삭제")
    public void deleteMessageFailUserNotReceiver() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId;
        User user = User.builder().id(senderId).build();
        doThrow(new UnAuthorizedException("본인에게 온 메시지만 삭제할 수 있습니다."))
                .when(messageFacade).deleteMessage(messageId, senderId);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("본인에게 온 메시지만 삭제할 수 있습니다.")));
    }

    @Test
    @DisplayName("메시지 삭제 실패 - 이미 삭제된 메시지")
    public void deleteMessageFailAlreadyDeleted() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId;
        User user = User.builder().id(senderId).build();
        doThrow(new MessageAlreadyDeletedException())
                .when(messageFacade).deleteMessage(messageId, senderId);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.delete(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(MessageAlreadyDeletedException.DEFAULT_MESSAGE)));
    }

    @Test
    @DisplayName("메시지 신고 성공")
    public void reportMessage() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId + "/reports";
        User user = User.builder().id(senderId).build();

        ReportReq reportReq = ReportReq.builder()
                .content(content)
                .build();
        ReportData reportData = ReportData.builder().build();
        doReturn(reportData).when(messageFacade).reportMessage(messageId, user.getId(), reportReq.getContent());


        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .content(gson.toJson(reportReq))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions.andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("메시지가 성공적으로 신고되었습니다.")))
                .andExpect(jsonPath("$.status", is(SUCCESS)));
    }

    @Test
    @DisplayName("메시지 신고 실패 - 비로그인 상태")
    public void reportMessageFailNullUser() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId + "/reports";
        ReportReq reportReq = ReportReq.builder()
                .content(content)
                .build();

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .content(gson.toJson(reportReq))
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is(UnAuthorizedException.DEFAULT_MESSAGE)));
    }

    @Test
    @DisplayName("메시지 신고 실패 - 서버 에러")
    public void reportMessageFailServerError() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId + "/reports";
        User user = User.builder().id(senderId).build();
        ReportReq reportReq = ReportReq.builder()
                .content(content)
                .build();
        doReturn(null).when(messageFacade).reportMessage(messageId, senderId, content);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .content(gson.toJson(reportReq))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is(ReportFailException.DEFAULT_MESSAGE)));
    }

    @Test
    @DisplayName("메시지 신고 실패 - 본인이 받지 않은 메시지")
    public void reportMessageFailUserNotReceiver() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId + "/reports";
        User user = User.builder().id(senderId).build();
        ReportReq reportReq = ReportReq.builder()
                .content(content)
                .build();
        doThrow(new UnAuthorizedException("본인이 받은 메시지만 신고할 수 있습니다."))
                .when(messageFacade).reportMessage(messageId, senderId, content);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .content(gson.toJson(reportReq))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("본인이 받은 메시지만 신고할 수 있습니다.")));
    }

    @Test
    @DisplayName("메시지 신고 실패 - 이미 신고됨")
    public void reportMessageFailAlreadyReported() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId + "/reports";
        User user = User.builder().id(senderId).build();
        ReportReq reportReq = ReportReq.builder()
                .content(content)
                .build();
        doThrow(new MessageAlreadyReportedException())
                .when(messageFacade).reportMessage(messageId, senderId, content);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .content(gson.toJson(reportReq))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is(MessageAlreadyReportedException.DEFAULT_MESSAGE)));
    }

    @Test
    @DisplayName("이모지 추가 성공")
    public void addEmoji() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId + "/emojis/" + emojiId;
        User user = User.builder().id(senderId).build();
        EmojiData emojiData = EmojiData.builder().build();
        doReturn(emojiData).when(messageFacade).addEmoji(messageId, user.getId(), emojiId);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("이모지가 성공적으로 변경되었습니다.")))
                .andExpect(jsonPath("$.status", is(SUCCESS)));
    }

    @Test
    @DisplayName("이모지 추가 실패 - 비로그인 상태")
    public void addEmojiFailNullUser() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId + "/emojis/" + emojiId;

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is(UnAuthorizedException.DEFAULT_MESSAGE)));
    }

    @Test
    @DisplayName("이모지 추가 실패 - 서버 에러")
    public void addEmojiFailServerError() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId + "/emojis/" + emojiId;
        User user = User.builder().id(senderId).build();
        doReturn(null).when(messageFacade).addEmoji(messageId, senderId, emojiId);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message", is(EmojiFailException.DEFAULT_MESSAGE)));
    }

    @Test
    @DisplayName("이모지 추가 실패 - 본인이 받지 않은 메시지")
    public void addEmojiFailUserNotReceiver() throws Exception {
        // given
        final String url = "/api/v1/messages/" + messageId + "/emojis/" + emojiId;
        User user = User.builder().id(senderId).build();
        doThrow(new UnAuthorizedException("본인이 받은 메시지에만 이모지를 달 수 있습니다."))
                .when(messageFacade).addEmoji(messageId, senderId, emojiId);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user", user);
                            return request;
                        })
        );

        // then
        resultActions
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("본인이 받은 메시지에만 이모지를 달 수 있습니다.")));
    }
}
