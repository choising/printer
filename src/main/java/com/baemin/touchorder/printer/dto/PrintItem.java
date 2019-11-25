package com.baemin.touchorder.printer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PrintItem {

    private String assetId;

    private String assetName;

    private String userNumber;

    private String userName;

    private String userDepartmentName;

    private String serialNumber;

}
