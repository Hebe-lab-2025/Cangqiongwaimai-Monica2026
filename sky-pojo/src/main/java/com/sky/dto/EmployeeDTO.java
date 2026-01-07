package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

@Data
/**
 * EmployeeDTO类，表示员工数据传输对象(Data Transfer Object)
 * 实现Serializable接口，使对象可以序列化和反序列化
 * 用于在系统不同层之间传递员工相关信息
 */

public class EmployeeDTO implements Serializable {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;
}
