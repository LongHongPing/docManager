package com.hp.docmanager.service;

import com.hp.docmanager.mapper.FileMapper;
import com.hp.docmanager.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:
 *
 * @Author hp long
 * @Date 2022/4/2 17:04
 */

public interface UserService {

    public void createUser(User user) throws Exception;
    public String checkUser(User user ) throws Exception;
    public int findUserID(String username) throws Exception;
    public boolean findUser(String username) throws Exception;
    public int isVip(String user_name) throws Exception;
}
