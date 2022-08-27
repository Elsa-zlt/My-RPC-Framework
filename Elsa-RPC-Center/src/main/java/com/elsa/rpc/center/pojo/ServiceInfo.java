package com.elsa.rpc.center.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : ServiceInfo  //类名
 * @Author : elsa //作者
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceInfo {

    private String ip;

    private int port;

}
