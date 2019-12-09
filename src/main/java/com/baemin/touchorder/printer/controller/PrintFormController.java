package com.baemin.touchorder.printer.controller;

import com.baemin.touchorder.printer.dto.PrintDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author seungmin
 */
@Slf4j
@Controller
public class PrintFormController {

    @GetMapping("/v1/print/form")
    public String test() {
        return "index";
    }

    @PostMapping("/v1/print/form")
    public String test(PrintDto printDto) {
        log.info("Hello : {}", printDto);
        return "index";
    }

}
