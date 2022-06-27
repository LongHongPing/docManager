package com.hp.docmanager.mapper;

import com.hp.docmanager.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Description:
 *
 * @Author hp long
 * @Date 2022/4/2 16:58
 */


public interface UserMapper {

    void createUser(User user) throws Exception;

    String checkUser(User user) throws Exception;

    Integer findUser(String username) throws Exception;

    Integer isVip(String username)throws Exception;
}
