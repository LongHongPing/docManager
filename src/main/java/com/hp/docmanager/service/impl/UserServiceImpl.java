package com.hp.docmanager.service.impl;

import com.hp.docmanager.mapper.UserMapper;
import com.hp.docmanager.model.User;
import com.hp.docmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:
 *
 * @Author hp long
 * @Date 2022/4/2 17:22
 */

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void createUser(User user) throws Exception{
        Boolean found = findUser(user.getUsername());
        if(!found) {
            userMapper.createUser(user);
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public String checkUser(User user ) throws Exception{
        return userMapper.checkUser(user);
    }

    @Override
    public int findUserID(String username) throws Exception{
        return userMapper.findUser(username);
    }

    @Override
    public boolean findUser(String username) throws Exception{
        Integer found = userMapper.findUser(username);
        return found != null && found >= 1;
    }

    @Override
    public int isVip(String user_name)throws Exception {
        return userMapper.isVip(user_name);
    }
}
