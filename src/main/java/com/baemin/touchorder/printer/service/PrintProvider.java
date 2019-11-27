package com.baemin.touchorder.printer.service;

import com.baemin.touchorder.printer.dto.PrintDto;

import java.util.List;

public interface PrintProvider {
    List<Long> print(PrintDto printDto);

    void print();

    void cutting();

    boolean isReady();

    void reconnection();
}
