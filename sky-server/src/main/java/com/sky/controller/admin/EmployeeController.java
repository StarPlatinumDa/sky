package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
// RequestMapping具有类属性，可以进行很多请求方法，GetMapping是它的一个延申，目的是提高阅读性
// 是一个通用的请求映射注解，支持所有HTTP方法（GET、POST、PUT等）。需要通过method属性显式指定具体的HTTP方法。
@Slf4j
@Api(tags = "员工相关接口")  // Swagger注解
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录")  // Swagger注解，描述作用
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        // 将数据的id放到 map中
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        // 工具类，JwtUtil 创建token   传递key和过期时间，调试时能看到值
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);
        // 令牌封装好后把数据封装，用对应的视图给前端界面
        // 用builder(VO对象上有builder注解)而不是new对象
        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出")
    public Result<String> logout() {
        return Result.success();
    }


    /**
     * 新增员工 先调用service进行逻辑处理，再在service的内部调用DAO(持久层)存入
     * 有嵌套，为了解耦
     * @param employeeDTO
     * @return
     */
    @PostMapping // /admin/employee已经在方法处加过了，所以不用多写
    @ApiOperation("新增员工")
    // @RequestBody自动将json格式反序列化为对象
    public Result save(@RequestBody EmployeeDTO employeeDTO){
//        System.out.println("当前线程的ID为:"+Thread.currentThread().getId());
        log.info("新增员工：{}",employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    /**
     * 员工表分页
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页")
    public Result page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页，参数为：{}",employeePageQueryDTO);
        PageResult result=employeeService.page(employeePageQueryDTO);
        return Result.success(result);
    }

    /**
     * 员工禁用账号
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("员工禁用账号")
    public Result statusSetting(@PathVariable Integer status,Long id){
        log.info("启用员工状态禁用:{},{}",status,id);
        employeeService.statusSetting(status,id);
        return Result.success();
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询员工信息")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("根据id查询员工信息:{}",id);
        Employee employee=employeeService.getById(id);

        return Result.success(employee);
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        log.info("修改员工信息,参数为{}",employeeDTO);
        employeeService.update(employeeDTO);

        return Result.success();
    }



}
