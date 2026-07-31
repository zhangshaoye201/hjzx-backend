package com.container.dataCollect.entity;

import lombok.Data;

@Data
public class TaskQueryDTO {

    private String category;

    private String taskName;

    private String unitNo;

    private Integer status;

    private String claimBy;

    private Integer claimed;
    
    private String unitName;
    
    private int page = 0;

    private int size = 10;
}

