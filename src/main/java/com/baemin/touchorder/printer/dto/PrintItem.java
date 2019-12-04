package com.baemin.touchorder.printer.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PrintItem {

    private long shopNumber;

    private String shopName;

    private String qrType;

    private String tableNumber;

    private String tableName;

    private String token;

    private String qrImageUrl;

}
