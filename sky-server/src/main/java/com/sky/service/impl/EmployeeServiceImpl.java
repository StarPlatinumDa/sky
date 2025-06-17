package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 进行md5加密，然后再进行比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //0代表 账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        // DTO便于解析前端数据，但是交给持久层存储时还是要实体类，所以需要转换
        Employee employee = new Employee();
//        employee.setName(employeeDTO.getName());
//        employee.setUsername(employeeDTO.getUsername());
//        employee.setPhone(employeeDTO.getPhone());
//        employee.setSex(employeeDTO.getSex());
//        employee.setIdNumber(employeeDTO.getIdNumber());
        // 对象属性拷贝  前提是属性名一直  源 -> 目标
        BeanUtils.copyProperties(employeeDTO,employee);
        // 设置账号状态1  用常量类表示
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        // 当前暂时获取不到当前管理员id，先固定
        // 从当前线程的存储空间中取出
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        // 持久层
        employeeMapper.insert(employee);
    }

    /**
     * 员工分页
     *
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult page(EmployeePageQueryDTO employeePageQueryDTO) {
        // 分页通过数据的limit来实现
        // select * from employee limit 0,10
        // 使用Pagehelper进行分页查询
        // 会跟后面的sql语句进行动态拼接(类似于mybatis的动态sql,动态拼接limit关键字和自动计算offset和count)
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        // 之所以没传参也能分页，是因为PageHelper先在类的ThreadLocal局部变量中存了传入的page页码和大小
        // 在分页前取出，动态地计算并拼接limit语句
        // 且在执行sql语句前会自动执行SELECT count(0) FROM employee得到总记录数
        Page<Employee> page=employeeMapper.page(employeePageQueryDTO);
        PageResult pageResult = new PageResult(page.getTotal(),page.getResult());
        return pageResult;
    }

    /**
     * 员工禁用
     * @param status
     * @param id
     */
    @Override
    public void statusSetting(Integer status, Long id) {
        // 本质是修改表中的status字段
        // update employee set status=? where id=?
        // 为了更通用，还是传实体，而不是只传两个参数
//        Employee employee = new Employee();
//        employee.setStatus(status);
//        employee.setId(id);
        // 因为有注解，所以可以用builder
        Employee employee = Employee.builder().status(status).id(id).build();


        employeeMapper.update(employee);
    }

    /**
     * 根据id得到员工信息
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        // 查到的数据中有密码，后台抹除一下
        employee.setPassword("***");
        return employee;

    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.update(employee);
    }
}
