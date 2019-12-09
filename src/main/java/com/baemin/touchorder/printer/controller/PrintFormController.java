package com.baemin.touchorder.printer.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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

}
