package com.olivejua.payservice.service;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceCancelTest {

    /**
     * 유저가 존재하지 않으면 예외를 던진다.
     * 유저가 탈퇴상태이면 예외를 던진다.
     * 요청 결제건이 존재하지 않으면 예외를 던진다.
     * 결제자와 요청자가 다르면 예외를 던진다.
     * 결제건이 이미 취소상태라면 예외를 던진다.
     * 결제대행사에 취소 요청한 결과 실패하면 예외를 던진다.
     * 결제건을 취소상태로 업데이트하고, 보유금액이 차감된 상태로 업데이트된다.
     */

}
