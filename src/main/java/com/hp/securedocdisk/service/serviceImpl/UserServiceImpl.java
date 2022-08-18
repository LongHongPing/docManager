package com.hp.securedocdisk.service.serviceImpl;


import com.hp.securedocdisk.mapper.UserMapper;
import com.hp.securedocdisk.model.User;
import com.hp.securedocdisk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value="userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper dao;

    public void createUser(User user) throws Exception{
        Boolean found = findUser(user.getUsername());
        if(!found)
            dao.createUser(user);
        else
            throw new RuntimeException();
    }

    public String checkUser(User user ) throws Exception{
        return dao.checkUser(user);
    }
    public int findUserID(String username) throws Exception{
        return dao.findUser(username);

    }
    public boolean findUser(String username) throws Exception{
        Integer found = dao.findUser(username);
        if(found==null || found<1)  return false;
        return true;
    }

    public int isVip(String user_name)throws Exception {
        return dao.isVip(user_name);
    }
}
