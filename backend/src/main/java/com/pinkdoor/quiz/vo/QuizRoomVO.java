package com.pinkdoor.quiz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 建房和查房的响应。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizRoomVO {

    private Long roomId;

    private String status;

    private Integer totalRounds;

    /**
     * 空间内的成员，固定两个人。
     */
    private List<Member> members;

    /**
     * 房间成员信息。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Member {

        private Long userId;

        private String nickname;

        private String avatar;
    }
}
