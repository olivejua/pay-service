package com.olivejua.payservice.controller;

import org.junit.jupiter.api.Test;

class PaymentControllerCreateTest {

    @Test
    void 유저가_활성상태가_아니라면_결제취소에_실패한다() {
        //given

        //when

        //then
    }

    @Test
    void 결제정보가_존재하지_않으면_결제취소에_실패한다() {
        //given

        //when

        //then
    }

    @Test
    void 유저의_현재_보유금액과_결제금액_합산금액이_최대한도금액을_초과한다면_결제취소에_실패한다() {
        //given

        //when

        //then
    }

    @Test
    void 유저의_머니결제금액이_요청월_기준_1달_결제최대한도금액을_초과하면_결제에_실패한다() {
        //given

        //when

        //then
    }

    @Test
    void 유저의_머니결제금액이_요청일자_기준_1일_결제최대한도금액을_초과하면_결제에_실패한다() {
        //given

        //when

        //then
    }

    @Test
    void 유저의_머니결제금액이1회_결제최대한도금액을_초과하면_결제에_실패한다() {
        //given

        //when

        //then
    }

    @Test
    void 결제대행사에서_실패응답이_오면_결제에_실패한다() {
        //given

        //when

        //then
    }

    @Test
    void 처리시간이_5초_초과되면_결제에_실패한다() {
        //given

        //when

        //then
    }

    @Test
    void 모든_조건을_충족하면_결제성공_후_유저_보유금액이_머니_결제금액만큼_업데이트된다() {
        //given

        //when

        //then
    }
}
