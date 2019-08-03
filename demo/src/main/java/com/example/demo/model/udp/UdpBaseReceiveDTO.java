package com.example.demo.model.udp;

import lombok.*;

/**
 * @ClassName UdpBaseReceiveDTO
 * @Author zs427
 * @Date 2019-7-30 17:59
 * @Version 1.0
 */
@Data
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class UdpBaseReceiveDTO {

    private String id;
    private String token;
    private String msg;

}
