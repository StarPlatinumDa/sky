package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行功能字段自动填充处理
 */
// 当前注解会加在什么位置
@Target(ElementType.METHOD)
// 指定注解的生命周期  RUNTIME 注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在(内存中的字节码)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    // 指定数据库操作类型(只有INSERT和UPDATE会触发)
    // 这个value()是注解的一个属性，即固定了value必须是OperationType的类型(而其内只有两个值)
    OperationType value();
}
