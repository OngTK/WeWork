package com.wework.global.dto.response;

/**
 * 공통 성공 응답 record
 * */
public record CommonSuccessResponseDto(boolean success) {
    public static CommonSuccessResponseDto ok(){
        return new CommonSuccessResponseDto(true);
    } // func end
} // record end
