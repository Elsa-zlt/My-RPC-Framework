package com.elsa.rpc.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName : RpcResponse  //类名
 * @Description : 封装的响应对象
 * @Author : elsa //作者
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse {

    /**
     * 响应ID
     */
    private String requestId;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 返回的结果
     */
    private Object result;

}
