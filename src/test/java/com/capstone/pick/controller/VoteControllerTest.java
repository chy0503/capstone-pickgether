package com.capstone.pick.controller;

import com.capstone.pick.config.TestSecurityConfig;
import com.capstone.pick.controller.form.VoteForm;
import com.capstone.pick.controller.form.VoteOptionFormDto;
import com.capstone.pick.domain.constant.Category;
import com.capstone.pick.domain.constant.DisplayRange;
import com.capstone.pick.dto.HashtagDto;
import com.capstone.pick.dto.UserDto;
import com.capstone.pick.dto.VoteDto;
import com.capstone.pick.dto.VoteOptionDto;
import com.capstone.pick.service.VoteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("View 컨트롤러 - 투표 게시글")
@Import(TestSecurityConfig.class)
@WebMvcTest(VoteController.class)
class VoteControllerTest {

    private final MockMvc mvc;

    @MockBean
    private VoteService voteService;

    public VoteControllerTest(@Autowired MockMvc mvc) {
        this.mvc = mvc;
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][GET] 홈페이지 - 정상 호출, 인증된 사용자")
    @Test
    void home() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/timeline"))
                .andExpect(redirectedUrl("/timeline"));
    }

    @DisplayName("[view][GET] 홈페이지 - 인증이 없을 땐 로그인 페이지로 이동")
    @Test
    void noLoginUser_home() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // TODO : 타임라인 조회의 경우 추후에 정렬과 페이징이 적용되면 테스트를 수정 해야한다.
    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][GET] 투표 게시글 조회 - 정상 호출, 인증된 사용자")
    @Test
    void timeLine() throws Exception {
        // given

        // when
        mvc.perform(get("/timeline"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("/page/timeLine"))
                .andExpect(model().attributeExists("votes"));

        // then
    }

    @DisplayName("[view][GET] 투표 게시글 조회 - 인증이 없을 땐 로그인 페이지로 이동")
    @Test
    void noLoginUser_timeline() throws Exception {
        // when
        mvc.perform(get("/timeline"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        // when
        then(voteService).shouldHaveNoInteractions();
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][POST] 새 투표 게시글 등록 - 정상 호출")
    @Test
    void saveVote() throws Exception {
        // given
        VoteForm voteForm = VoteForm.builder()
                .title("new title")
                .content("new content # #hash1 #hash33")
                .category(Category.WORRY)
                .isMultiPick(true)
                .expiredAt(LocalDateTime.now())
                .voteOptions(List.of(VoteOptionFormDto.builder().content("new option1").imageLink("/link/image1.png").build(),
                        VoteOptionFormDto.builder().content("new option2").imageLink("/link/image2.png").build()))
                .build();

        //when
        mvc.perform(post("/form")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("voteForm", voteForm)
                        .with(csrf())
                ).andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/timeLine"))
                .andExpect(redirectedUrl("/timeLine"));

        // then
        then(voteService).should().saveVote(any(VoteDto.class), Mockito.<VoteOptionDto>anyList(), Mockito.<HashtagDto>anyList());
    }

    @DisplayName("[view][GET] 투표 게시글 수정 페이지 - 인증이 없을 땐 로그인 페이지로 이동")
    @Test
    void noLoginUser_editVote() throws Exception {
        // given
        long voteId = 1L;

        // when & then
        mvc.perform(get("/" + voteId + "/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
        then(voteService).shouldHaveNoInteractions();
    }

    @WithMockUser
    @DisplayName("[view][GET] 투표 게시글 수정 페이지 - 정상 호출, 인증된 사용자")
    @Test
    void editVote_GET() throws Exception {
        // given
        long voteId = 1L;

        UserDto userDto = UserDto.builder()
                .userId("user")
                .userPassword("password")
                .email("email@email.com")
                .nickname("nick")
                .memo("memo")
                .birthday(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        VoteDto voteDto = VoteDto.builder()
                .userDto(userDto)
                .title("title")
                .content("new content")
                .voteOptions(List.of(VoteOptionFormDto.builder().content("new option1").imageLink("/link/image1.png").build(),
                        VoteOptionFormDto.builder().content("new option2").imageLink("/link/image2.png").build()))
                .category(Category.WORRY)
                .expiredAt(LocalDateTime.now().plusDays(5))
                .createAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .isMultiPick(true)
                .displayRange(DisplayRange.PUBLIC)
                .build();

        given(voteService.getVote(voteId)).willReturn(voteDto);

        // when
        mvc.perform(get("/" + voteId + "/edit"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(view().name("/page/editForm"))
//                .andExpect(model().attribute("voteForm", voteForm))
//                .andExpect(model().attribute("optionDtos", optionDtos))
                .andExpect(model().attribute("voteDto", voteDto));

        // then
        then(voteService).should().getVote(voteId);
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][POST] 투표 게시글 수정 - 정상 호출")
    @Test
    void editVote_POST() throws Exception {
        // given
        long voteId = 1L;

        VoteForm voteForm = VoteForm.builder()
                .title("update title123")
                .content("update content # #hash1")
                .category(Category.FREE)
                .isMultiPick(true)
                .expiredAt(LocalDateTime.now())
                .voteOptions(List.of(VoteOptionFormDto.builder().content("new option1").imageLink("/link/image1.png").build(),
                        VoteOptionFormDto.builder().content("new option2").imageLink("/link/image2.png").build()))
                .build();

        //when
        mvc.perform(post("/" + voteId + "/edit")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .flashAttr("voteForm", voteForm)
                        .with(csrf())
                ).andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/timeLine"))
                .andExpect(redirectedUrl("/timeLine"));

        // then
        then(voteService).should().updateVote(any(Long.class), any(VoteDto.class), Mockito.<VoteOptionDto>anyList(), Mockito.<HashtagDto>anyList());
    }

    @WithUserDetails(value = "user", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @DisplayName("[view][POST] 투표 게시글 삭제 - 정상 호출")
    @Test
    void deleteVote_POST() throws Exception {
        // given
        long voteId = 1L;
        String userId = "user";
        willDoNothing().given(voteService).deleteVote(voteId, userId);

        // when
        mvc.perform(post("/" + voteId + "/delete")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/timeline"))
                .andExpect(redirectedUrl("/timeline"));

        // then
        then(voteService).should().deleteVote(voteId, userId);
    }
}