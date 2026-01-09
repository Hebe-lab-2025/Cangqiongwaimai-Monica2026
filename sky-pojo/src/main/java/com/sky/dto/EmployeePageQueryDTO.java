package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 员工分页查询数据传输对象
 * 该类用于封装员工分页查询的参数，实现了Serializable接口以支持序列化
 */
@Data
public class EmployeePageQueryDTO implements Serializable {
    //员工姓名
    private String name;

    //页码
    private int page;

    //每页显示记录数
    private int pageSize;
}
