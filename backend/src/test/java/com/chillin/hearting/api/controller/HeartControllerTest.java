package com.chillin.hearting.api.controller;

import com.chillin.hearting.api.data.HeartData;
import com.chillin.hearting.api.data.HeartDetailData;
import com.chillin.hearting.api.data.HeartListData;
import com.chillin.hearting.api.service.facade.HeartFacade;
import com.chillin.hearting.db.domain.Heart;
import com.chillin.hearting.db.domain.User;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class HeartControllerTest {

    @InjectMocks
    private HeartController heartController;

    @Mock
    private HeartFacade heartFacade;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(heartController)
                .build();
    }

    @Test
    @DisplayName("하트 도감 조회 - 비로그인")
    void 도감조회_비로그인() throws Exception {
        // given
        final String url = "/api/v1/hearts";

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("하트 도감 조회 - 로그인")
    void 도감조회_로그인() throws Exception {

        // given
        final String url = "/api/v1/hearts";
        List<HeartData> heartDataList = new ArrayList<>();
        HeartData defaultHeartData = HeartData.of(createDefaultHeart(1L));
        HeartData specialHeartData = HeartData.of(createDefaultHeart(7L));
        specialHeartData.setLock();

        heartDataList.add(defaultHeartData);
        heartDataList.add(specialHeartData);
        HeartListData data = HeartListData.builder()
                .heartList(heartDataList)
                .build();

        // mocking
        when(heartFacade.findAllHearts(any())).thenReturn(data);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.heartList[0].isLocked", is(false)))
                .andExpect(jsonPath("$.data.heartList[1].isLocked", is(true)));
    }


    @Test
    @DisplayName("하트 조회 - 비로그인")
    void 유저하트조회_비로그인() throws Exception {
        // given
        final String url = "/api/v1/hearts/user-hearts";

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk());
    }

    @Test
    @DisplayName("하트 조회 - 로그인")
    void 유저하트조회_로그인() throws Exception {

        // given
        final String url = "/api/v1/hearts/user-hearts";
        List<HeartData> heartDataList = new ArrayList<>();
        heartDataList.add(HeartData.of(createDefaultHeart(1L)));
        heartDataList.add(HeartData.of(createSpecialHeart(6L)));

        // mocking
        when(heartFacade.findMessageHearts(any())).thenReturn(heartDataList);

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.heartList[0].isLocked", is(false)))
                .andExpect(jsonPath("$.data.heartList[1].isLocked", is(false)));
    }

    @Test
    @DisplayName("하트 도감 상세 조회")
    void findHeartDetail() throws Exception {
        // given
        final String url = "/api/v1/hearts/1";
        HeartDetailData heartDetailData = HeartDetailData.of(createDefaultHeart(1L));

        // mocking
        doReturn(heartDetailData).when(heartFacade).findHeartDetail(any(),anyLong());

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.get(url)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.data.heartId", is(1)))
                .andExpect(jsonPath("$.data.name", is(heartDetailData.getName())))
                .andExpect(jsonPath("$.data.heartUrl", is(heartDetailData.getHeartUrl())))
                .andExpect(jsonPath("$.data.shortDescription", is(heartDetailData.getShortDescription())))
                .andExpect(jsonPath("$.data.longDescription", is(heartDetailData.getLongDescription())))
                .andExpect(jsonPath("$.data.type", is(heartDetailData.getType())))
                .andExpect(jsonPath("$.data.acqCondition", is(heartDetailData.getAcqCondition())))
                .andExpect(jsonPath("$.data.isLocked", is(heartDetailData.getIsLocked())))
                .andExpect(jsonPath("$.data.isAcq", is(heartDetailData.getIsAcq())))
                .andExpect(jsonPath("$.data.conditions", is(heartDetailData.getConditions())))
        ;
    }

    @Test
    @DisplayName("스페셜 하트 획득")
    void saveUserHearts() throws Exception {
        // given
        final String url = "/api/v1/hearts/user-hearts/6";
        User user = User.builder().id("id").build();

        // mocking
        doNothing().when(heartFacade).saveUserHearts(anyString(),anyLong());

        // when
        final ResultActions resultActions = mockMvc.perform(
                MockMvcRequestBuilders.post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(request -> {
                            request.setAttribute("user",user);
                            return request;
                        })
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("하트 획득에 성공했습니다.")))
        ;
    }

    public Heart createDefaultHeart(Long id) {
        Heart heart = Heart.builder()
                .id(id)
                .name("호감 하트")
                .imageUrl("test.com")
                .shortDescription("짧은 설명 !")
                .longDescription("호감의 탄생 스토리")
                .acqCondition("기본 제공")
                .type("DEFAULT")
                .build();

        return heart;
    }

    public Heart createSpecialHeart(Long id) {
        Heart heart = Heart.builder()
                .id(id)
                .name("행성 하트")
                .imageUrl("universe.com")
                .shortDescription("우주에 단 하나 뿐인 너 !")
                .longDescription("우주의 탄생 스토리")
                .acqCondition("특정인에게 5회 이상 메시지 전송")
                .type("SPECIAL")
                .build();

        return heart;
    }
}
