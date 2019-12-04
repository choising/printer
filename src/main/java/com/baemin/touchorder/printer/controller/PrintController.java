package com.baemin.touchorder.printer.controller;

import com.baemin.touchorder.printer.dto.ApiResponse;
import com.baemin.touchorder.printer.dto.PrintDto;
import com.baemin.touchorder.printer.dto.PrintItem;
import com.baemin.touchorder.printer.service.PrintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class PrintController {

    @Autowired
    private PrintService printService;

    /***
     * 배민오더 어드민 인쇄 요청을 받는 Controller
     *
     * @param printDto
     * @return
     */
    @PostMapping("/v1/print")
    public ApiResponse<Boolean> print(@RequestBody PrintDto printDto) {

        log.info("[Print-Request] Request QR Print - itemCount: {}, repeatCount: {}", printDto.getPrintItems().size(), printDto.getCount());

        // call print service
        List<String> failList = printService.print(printDto);

        // failList 가 비어있지 않으면 Error
        if (!failList.isEmpty()) {
            log.info("[Print-Error] QR Print Error! - failList: {}", failList);
            new ApiResponse<>(false, String.valueOf(failList));
        }

        // success
        log.info("[Print-Ok] QR Print Success - itemCount: {}, repeatCount: {}", printDto.getPrintItems().size(), printDto.getCount());
        return new ApiResponse<>(true);
    }

    /***
     * 프린터 연결상태 health check controller
     *
     * @return
     */
    @GetMapping("/v1/print/check")
    public ApiResponse<Boolean> check() {
        boolean isReady = printService.isReady();
        return isReady ? new ApiResponse<>(true) : new ApiResponse<>(false, "connect fail.");
    }

    @PostMapping("/v1/print/test")
    public ApiResponse<Boolean> test(@RequestBody PrintItem printItem) {
        printService.testPrint(printItem);
        return new ApiResponse<>(true);
    }

    @GetMapping("/v1/print/cut")
    public Boolean cut() {
        printService.cutting();
        return true;
    }
}
