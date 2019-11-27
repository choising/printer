package com.baemin.touchorder.printer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PrintItem {

    private long qrSeq;

    private String shopName;

    private String qrType;

    private String tableNumber;

    private String tableName;

    private String token;

    private String qrImageUrl;

}
