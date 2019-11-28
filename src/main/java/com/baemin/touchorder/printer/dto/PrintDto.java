package com.baemin.touchorder.printer.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PrintDto {

    private List<PrintItem> printItems;

    private int count;

}
