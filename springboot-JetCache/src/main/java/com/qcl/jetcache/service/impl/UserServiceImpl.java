package com.qcl.jetcache.service.impl;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.qcl.jetcache.domain.User;
import com.qcl.jetcache.service.IUserService;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements IUserService {

    @CreateCache(name = "user_cache_", expire = 3600, cacheType = CacheType.BOTH, localLimit = 50)
    private Cache<Long, User> userCache;

    @Override
    public User getUserById(Long userId) {
        User user = userCache.get(userId);
        if (user != null) {
            return user;
        }
        user = new User(userId, "legendTest" + System.currentTimeMillis(), 18);
        userCache.put(userId, user);
        return user;
    }

    @Override
    public void updateUser(User user) {
        userCache.put(user.getUserId(), user);
        System.out.println("updateUser:" + user);
    }

    @Override
    public void deleteUser(Long userId) {
        userCache.remove(userId);
        System.out.println("deleteUser :" + userId);
    }
}