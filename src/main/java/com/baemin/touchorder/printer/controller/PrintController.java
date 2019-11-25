package com.baemin.touchorder.printer.controller;

import com.baemin.touchorder.printer.dto.PrintDto;
import com.baemin.touchorder.printer.service.PrintProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/w1/print")
public class PrintController {

    @Autowired
    private PrintProvider printService;

    @PostMapping
    public Boolean print(@RequestBody PrintDto printDto) {
        List<String> failList = printService.print(printDto);
        return true;
    }

    @GetMapping
    public Boolean print() {
        printService.print();
        return true;
    }

    @GetMapping("/check")
    public Boolean check() {
        boolean isReady = printService.isReady();
        return true;
    }

    @GetMapping("/reconnection")
    public Boolean reconnection() {
        printService.reconnection();
        return true;
    }

    @GetMapping("/cut")
    public Boolean cut() {
        printService.cutting();
        return true;
    }
}
