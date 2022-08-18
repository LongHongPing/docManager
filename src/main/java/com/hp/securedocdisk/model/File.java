package com.hp.securedocdisk.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component("file")
@Scope("prototype")
public class File {
	
	private int id;
	private String filename;
	private String filepath;
	private String filesize;
	private Date createtime;

	private int canshare;
	private int user_id;
	private String MD5;

}
