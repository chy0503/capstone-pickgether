package com.capstone.pick.controller;

import com.capstone.pick.controller.form.CommentForm;
import com.capstone.pick.dto.CommentDto;
import com.capstone.pick.exeption.UserMismatchException;
import com.capstone.pick.security.VotePrincipal;
import com.capstone.pick.service.VoteCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class VoteCommentsController {

    private final VoteCommentService voteCommentService;

    /**
     * 댓글을 조회한다
     *
     * @param voteId 게시글 id
     * @return 댓글 목록 뷰
     */
    @GetMapping("/{voteId}/comments")
    public String readComments(@AuthenticationPrincipal VotePrincipal votePrincipal, @PathVariable Long voteId, Model model) {
        List<CommentDto> comments = voteCommentService.readComment(voteId);
        model.addAttribute("voteId", voteId);
        model.addAttribute("userId", votePrincipal.toDto().getUserId());
        model.addAttribute("comments", comments);
        return "page/comments";
    }

    /**
     * 댓글을 작성한다
     *
     * @param votePrincipal 사용자
     * @param voteId        게시글 id
     * @param commentForm   작성내용
     * @return redirection to GET
     */
    @PostMapping("/{voteId}/comments")
    public String saveComment(@AuthenticationPrincipal VotePrincipal votePrincipal,
                              @PathVariable Long voteId, CommentForm commentForm) {

        voteCommentService.saveComment(commentForm.toDto(votePrincipal.toDto()));
        return "redirect:/" + voteId + "/comments";
    }

    /**
     * 댓글을 수정한다
     *
     * @param votePrincipal 사용자
     * @param voteId        게시글 id
     * @param commentId     댓글 id
     * @param commentForm   작성내용
     * @return redirection to GET
     */
    @PostMapping("/{voteId}/comments/{commentId}/edit")
    public String updateComment(@AuthenticationPrincipal VotePrincipal votePrincipal,
                                @PathVariable Long voteId, @PathVariable Long commentId,
                                CommentForm commentForm) throws UserMismatchException {

        voteCommentService.updateComment(commentId, commentForm.toDto(votePrincipal.toDto()));
        return "redirect:/" + voteId + "/comments";
    }

    /**
     * 댓글을 삭제한다
     *
     * @param votePrincipal 사용자
     * @param voteId        게시글 id
     * @param commentId     투표 댓글 id
     * @return redirection to GET
     */
    @PostMapping("/{voteId}/comments/{commentId}/delete")
    public String deleteComment(@AuthenticationPrincipal VotePrincipal votePrincipal,
                                @PathVariable Long voteId, @PathVariable Long commentId) throws UserMismatchException {
        voteCommentService.deleteComment(commentId, votePrincipal.toDto().getUserId());
        return "redirect:/" + voteId + "/comments";
    }
}
