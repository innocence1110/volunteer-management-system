package com.volunteer.config;

import com.volunteer.dto.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
        return Result.error("图片大小超过限制（最大 10MB），请压缩后重新上传");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.error("服务器错误: " + e.getMessage());
    }
}
