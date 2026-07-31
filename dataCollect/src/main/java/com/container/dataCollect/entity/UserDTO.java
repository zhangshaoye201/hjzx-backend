package com.container.dataCollect.entity;

import lombok.Data;

@Data
public class UserDTO {
	
    private String loginName;
    private String searchName;
    
    private int role;

    private int page = 0;

    private int size = 10;
}
