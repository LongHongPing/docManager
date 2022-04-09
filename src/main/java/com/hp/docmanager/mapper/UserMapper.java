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
@Mapper
public interface UserMapper {

    @Insert("INSERT INTO user(username,password) VALUES(#{username},#{password})")
    public void createUser(User user) throws Exception;

    @Select("SELECT user.username FROM user WHERE username=#{username} AND password=#{password}")
    public String checkUser(User user) throws Exception;

    @Select("SELECT user.id FROM user WHERE username=#{username}")
    public Integer findUser(String username) throws Exception;

    @Select("SELECT isvip FROM user WHERE username=#{value}")
    public Integer isVip(String user_name)throws Exception;
}
