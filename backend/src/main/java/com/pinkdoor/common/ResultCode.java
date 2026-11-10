package com.pinkdoor.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 业务错误码，数值同时用作 HTTP 状态码。
 */
@Getter
@RequiredArgsConstructor
public enum ResultCode {

    SUCCESS(200, "Success"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Please log in first."),
    INVALID_CREDENTIALS(401, "That nickname or secret code did not match."),
    NOT_FOUND(404, "Not found"),
    INTERNAL_ERROR(500, "Something went wrong, please try again later.");

    private final int code;
    private final String message;
}
