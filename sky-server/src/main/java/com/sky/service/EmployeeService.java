package com.sky.service;

import com.sky.dto.EmployeeDTO;

public interface EmployeeService {
    /**
     * 新增员工
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

}
