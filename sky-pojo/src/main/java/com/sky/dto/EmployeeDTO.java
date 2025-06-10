package com.sky.dto;

import lombok.Data;

import java.io.Serializable;
/*
实现了Serializable接口的类可以被ObjectOutputStream转换为字节流  即将对象转化为字节流方便存储
同时也可以通过ObjectInputStream再将其解析为对象
 */
@Data
public class EmployeeDTO implements Serializable {

    private Long id;

    private String username;

    private String name;

    private String phone;

    private String sex;

    private String idNumber;

}
