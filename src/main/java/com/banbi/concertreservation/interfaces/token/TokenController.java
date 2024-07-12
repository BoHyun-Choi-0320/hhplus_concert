package com.banbi.concertreservation.interfaces.token;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/")
public class TokenController {

    /*
        유저 토큰 발급 API
        - 활성토큰만료 스케줄러
     */
    @GetMapping("/token/{uuid}")
    public void getToken(@PathVariable String uuid){

    }
}
