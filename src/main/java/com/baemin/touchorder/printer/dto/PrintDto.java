package com.baemin.touchorder.printer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class PrintDto {

    private List<PrintItem> printItems;

    private int count;

}
