package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;

@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @param openid
     * @return
     */
    @Select("select * from user where openid=#{openid}")
    User getByOpenid(String openid);


    void insert(User user);

    @Select("select * from user where id=#{userId};")
    User getById(Long userId);

    /**
     * 历史用户数
     * @param date
     * @return
     */
    @Select("select count(id) from user where create_time<#{date}")
    Integer countLessThanDate(LocalDate date);

    /**
     * 当天新增用户数
     * @param begin
     * @param end
     * @return
     */
    @Select("select count(id) from user where create_time<#{end} and create_time>=#{begin}")
    Integer countOneDay(LocalDate begin, LocalDate end);
}
