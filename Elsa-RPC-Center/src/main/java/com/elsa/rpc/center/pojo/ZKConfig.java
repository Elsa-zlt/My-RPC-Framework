package com.elsa.rpc.center.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : ZKConfig  //类名
 * @Author : elsa //作者
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZKConfig {

    private String connectStr;

    private int sessionTimeoutMs;

    private int connectionTimeoutMs;

    private String namespace;

}
