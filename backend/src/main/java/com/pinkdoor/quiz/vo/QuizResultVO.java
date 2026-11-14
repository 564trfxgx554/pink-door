package com.pinkdoor.quiz.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 一局结束后的结果页数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultVO {

    private Integer matched;

    private Integer total;

    /**
     * 默契度，0 - 100。
     */
    private Integer score;

    private String text;

    private List<Detail> details;

    /**
     * 每一题的回顾。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {

        private Integer roundNo;

        private String content;

        private List<String> options;

        private List<AnswerItem> answers;

        private Boolean matched;
    }

    /**
     * 某个人在某题的答案。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerItem {

        private Long userId;

        private String nickname;

        /**
         * 未答为 -1。
         */
        private Integer optionIndex;
    }
}
