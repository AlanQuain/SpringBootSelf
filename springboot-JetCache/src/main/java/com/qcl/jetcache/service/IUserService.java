package com.qcl.jetcache.service;

import com.qcl.jetcache.domain.User;

public interface IUserService {

    User getUserById(Long userId);

    void updateUser(User user);

    void deleteUser(Long userId);
}