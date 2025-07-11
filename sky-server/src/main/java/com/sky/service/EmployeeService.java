package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 员工分页
     *
     * @param employeePageQueryDTO
     * @return
     */
    PageResult page(EmployeePageQueryDTO employeePageQueryDTO);

    void statusSetting(Integer status, Long id);

    /**
     * 根据id得到员工信息
     * @param id
     * @return
     */
    Employee getById(Long id);

    void update(EmployeeDTO employeeDTO);
}
