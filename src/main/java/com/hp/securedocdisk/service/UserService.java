package com.hp.securedocdisk.service;


import com.hp.securedocdisk.model.User;

public interface UserService {
	void createUser(User user) throws Exception;
	
	String checkUser(User user ) throws Exception;

    int findUserID(String username) throws Exception;

    boolean findUser(String username) throws Exception;

	int isVip(String user_name)throws Exception;
}
