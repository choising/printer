package com.baemin.touchorder.printer.controller;

import com.baemin.touchorder.printer.dto.PrintDto;
import com.baemin.touchorder.printer.service.PrintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @author seungmin
 */
@Slf4j
@Controller
public class PrintFormController {

    @Autowired
    private PrintService printService;

    @GetMapping("/v1/print/form")
    public String test() {
        return "success ";
    }

    @PostMapping("/v1/print/form")
    public String test(PrintDto printDto) {
        log.info("[Print-Request] Request QR Print - itemCount: {}, repeatCount: {}", printDto.getPrintItems().size(), printDto.getCount());

        // call print service
        List<String> failList = printService.print(printDto);

        // failList 가 비어있지 않으면 Error
        if (!failList.isEmpty()) {
            log.info("[Print-Error] QR Print Error! - failList: {}", failList);
            return "fail";
        }

        // success
        log.info("[Print-Ok] QR Print Success - itemCount: {}, repeatCount: {}", printDto.getPrintItems().size(), printDto.getCount());
        return "success";
    }

}
