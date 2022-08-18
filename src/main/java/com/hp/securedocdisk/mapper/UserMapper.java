package com.hp.securedocdisk.mapper;

import com.hp.securedocdisk.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {

    @Insert("INSERT INTO user(username,password,isvip) VALUES(#{username},#{password},#{isvip})")
    void createUser(User user) throws Exception;

    @Select("SELECT user.username FROM user WHERE username=#{username} AND password=#{password}")
    String checkUser(User user) throws Exception;

    @Select("SELECT user.id FROM user WHERE username=#{username}")
    Integer findUser(String username) throws Exception;

    @Select("SELECT isvip FROM user WHERE username=#{value}")
    Integer isVip(String user_name)throws Exception;
}
