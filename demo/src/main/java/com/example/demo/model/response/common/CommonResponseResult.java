package com.example.demo.model.response.common;

import lombok.*;

@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CommonResponseResult<T> extends ResponseResult {

    T Data;

    public CommonResponseResult(ResultCode resultCode, T data) {
        super(resultCode);
        this.Data = data;
    }

}
