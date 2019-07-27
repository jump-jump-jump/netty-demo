package com.example.demo.model.dto;

import lombok.*;

/**
 * @ClassName UserDTO
 * @Author zs427
 * @Date 2019-7-27 10:09
 * @Version 1.0
 */
@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String name;
    private Boolean sex;
    private Integer age;

}
