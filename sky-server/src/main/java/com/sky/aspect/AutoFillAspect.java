package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面类，实现公共字段填充      切入点+通知
 */
@Aspect// 切面注解
@Component
@Slf4j // 记录日志
public class AutoFillAspect {
    /**
     * 切入点(对哪些类的哪些方法进行拦截)
     */
    // 第一个星是所有返回类型   mapper后的第一个*是所有类   第二个是所有方法 括号内..匹配所有参数类型
    // 同时要满足被自定义注解注释
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}


    // 前置通知，在执行INSERT方法前就要为公共字段赋值
    // 指定切入点（匹配到切点表达式时执行通知的方法）
    // 通过连接点(JoinPoint)直到当前哪个方法被拦截，以及被拦截到方法的参数值和参数类型
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段自动赋值");
        // 1.获得拦截的方法是update还是insert,因为update只需要赋值updateUser和updateTime
        // 而insert还需要给create的两个字段赋值

        // 都是反射操作
        // 从普通签名转型成方法签名子接口，因为当前被拦截的其实是方法（注意这里的MethodSignature是aspectj.lang.reflect包下的）
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();// 方法签名对象
        // 获得方法上的的注解对象
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        // 获得注解的操作类型
        OperationType type = autoFill.value();

        // 2.获取当前方法的参数，即实体对象例如employee
        // 约定在写方法时把实体放在第一个，这样这里就只用获取第一个参数
        Object[] args = joinPoint.getArgs();
        // 没有参数的异常情况
        if(args==null||args.length==0){
            return;
        }
        // 获取第一个参数，即约定的实体对象
        Object entity = args[0];

        // 3.准备赋值数据，根据类型通过反射给公共属性赋值
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        if(type==OperationType.INSERT){
            // 为4个公共字段赋值
            // 先用实体类对象获取他的class对象,再获取内部的方法
            Class<?> clazz = entity.getClass();
            try {
                Method setCreateTime = clazz.getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
                Method setCreateUser = clazz.getDeclaredMethod("setCreateUser",Long.class);
                Method setUpdateTime = clazz.getDeclaredMethod("setUpdateTime",LocalDateTime.class);
                Method setUpdateUser = clazz.getDeclaredMethod("setUpdateUser",Long.class);

                setCreateTime.invoke(entity,now);
                setCreateUser.invoke(entity,currentId);
                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (type == OperationType.UPDATE) {
            // 为两个公共字段赋值
            Class<?> clazz = entity.getClass();
            try {
                Method setUpdateTime = clazz.getDeclaredMethod("setUpdateTime",LocalDateTime.class);
                Method setUpdateUser = clazz.getDeclaredMethod("setUpdateUser",Long.class);

                setUpdateTime.invoke(entity,now);
                setUpdateUser.invoke(entity,currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
