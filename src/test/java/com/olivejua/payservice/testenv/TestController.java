package com.olivejua.payservice.testenv;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test/timeout")
    public String timeoutTestApi() {
        return "OK";
    }
}
