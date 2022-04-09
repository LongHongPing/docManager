package com.hp.docmanager.model;

import lombok.Data;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Description:
 *
 * @Author hp long
 * @Date 2022/4/2 16:46
 */

@Component("user")
@Scope("prototype")
@Data
public class User {
    private int id;
    private String password;
    private String username;
    private int isvip = 1;
}
