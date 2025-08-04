package com.sky.service.impl;

import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Transactional
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {
        // 方法1：直接输入开头结尾日期，在数据库中用Date函数把order_time转化成日期   再group by得到每日的营业额
        // 不可用，因为有的日期数据库中未存储时，查询就没有结果，也就是说一个月只有两天的数据，最终就只能查出两行，而这两行是不知道与哪个日期对应的(也可以把Date函数的日期也查出进行对应，但太复杂)

        // 方法2：在java中根据开头和结尾直接构造每天的日期列表，然后根据列表一条一条进行查询(用 当天<=order_time<=第二天可以走索引  而Date函数后反而无法走索引)
        ArrayList<LocalDate> localDates = new ArrayList<>();
        ArrayList<Double> amountList = new ArrayList<>();
        for(LocalDate d=begin;!d.equals(end.plusDays(1));d=d.plusDays(1)){
            localDates.add(d);
        }
        Integer status=5;// 只统计已完成的订单
        for (LocalDate date : localDates) {
            Double amount=orderMapper.getSumByDate(date,date.plusDays(1),status);
            amount= amount==null ? 0.0 : amount;
            amountList.add(amount);
        }
        // lang3包下的StringUtils
        String dateList= StringUtils.join(localDates,",");
        // 两种转化为String的方法
        String turnoverList=amountList
                .stream()
                .map(amount->amount.toString())
                .collect(Collectors.joining(","));

        TurnoverReportVO turnoverReportVO = TurnoverReportVO
                .builder()
                .dateList(dateList)
                .turnoverList(turnoverList).build();

        return turnoverReportVO;
    }
}
