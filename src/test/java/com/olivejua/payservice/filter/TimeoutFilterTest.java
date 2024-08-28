package com.olivejua.payservice.filter;

import com.olivejua.payservice.testenv.TestController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class TimeoutFilterTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TestController testController;

    @Test
    void 처리시간이_5초_초과되면_결제에_실패한다() throws Exception {
        //given
        when(testController.timeoutTestApi()).thenAnswer(invocation -> {
            Thread.sleep(6000);
            return "OK";
        });

        //when & then
        mockMvc.perform(get("/test/timeout")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("PAYMENT_TIMEOUT"))
                .andExpect(jsonPath("$.message").value("Payment processing time exceeded the limit of 5 seconds."));
    }
}
