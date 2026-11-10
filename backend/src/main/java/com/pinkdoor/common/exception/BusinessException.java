package com.pinkdoor.common.exception;

import com.pinkdoor.common.ResultCode;
import lombok.Getter;

import java.io.Serial;

/**
 * 可预期的业务异常，错误码同时用作 HTTP 状态码。
 */
@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
