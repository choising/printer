package com.baemin.touchorder.printer.dto;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author seungmin
 */
@Getter
public class ApiResponse<T> implements Serializable {

    private static final String STATUS_CODE_OK = "00000";

    private static final String STATUS_CODE_ERROR = "99999";

    private String statusCode;

    private T data;

    private String errorMessage;

    // success
    public ApiResponse(T data) {
        this.statusCode = STATUS_CODE_OK;
        this.data = data;
        this.errorMessage = StringUtils.EMPTY;
    }

    // fail
    public ApiResponse(T data, String errorMessage) {
        this.statusCode = STATUS_CODE_ERROR;
        this.data = data;
        this.errorMessage = errorMessage;
    }

}
