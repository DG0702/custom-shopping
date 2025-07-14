package com.example.shopping.domain.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ExceptionCode {
    // 400 BAD_REQUEST
    INVALID_USER_ROLE(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 권한입니다"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 상품 요청입니다."),
    PRICE_OR_QUANTITY_REQUIRED(HttpStatus.BAD_REQUEST, "가격과 수량은 필수값입니다."),

    // 401 UNAUTHORIZED
    UNAUTHORIZED_REQUEST(HttpStatus.UNAUTHORIZED, "Spring Security 인증 통과 오류"),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 틀렸습니다"),

    // 403 FORBIDDEN
    FORBIDDEN_REQUEST(HttpStatus.FORBIDDEN, "Spring Security 인과 통과 요류"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "해당 작업에 대한 권한이 없습니다."),

    // 404 NOT_FOUND
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다"),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일을 찾을 수 없습니다."),
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니에 등록된 상품을 찾을 수 없습니다."),
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "상품 재고가 0개입니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을수 없습니다."),

    // 409 CONFLICT
    ALREADY_EXISTS_EMAIL(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다"),
    PRODUCT_OUT_OF_STOCK(HttpStatus.CONFLICT, "상품 재고가 부족합니다"),
    ALREADY_ORDER_CANCEL(HttpStatus.CONFLICT,"이미 취소된 주문입니다."),
    ALREADY_ORDERING(HttpStatus.CONFLICT,"현재 다른 요청에서 해당 상품의 주문을 처리중입니다. 잠시 후 다시 시도해 주세요"),

    // 500
    REDIS_LOCK_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR,"락 획득 중 인터럽트가 발생하였습니다.");


    private final HttpStatus httpStatus;
    private final String message;

    ExceptionCode(HttpStatus httpStatus, String message){
        this.httpStatus = httpStatus;
        this.message = message;
    }
}