package com.example.demo.model.response.common;

import lombok.*;

import java.util.List;

/**
 * @ClassName CommonPage
 * @Author zs427
 * @Date 2019-7-22 16:45
 * @Version 1.0
 */
@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class CommonPage<T> {

    private Long TotalPage;
    private Long TotalElements;
    private List<T> Data;

}
