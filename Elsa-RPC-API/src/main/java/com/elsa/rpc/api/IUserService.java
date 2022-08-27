package com.elsa.rpc.api;

import com.elsa.rpc.pojo.User;

/**
 * 用户服务
 */
public interface IUserService {

    /**
     * 根据ID查询用户
     *
     * @param id
     * @return
     */
    User getById(int id);

}
