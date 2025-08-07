package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;

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

    /**
     * 用户数量统计接口
     * @param begin
     * @param end
     * @return
     */
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> localDates = new ArrayList<>();
        ArrayList<Integer> totalUser = new ArrayList<>();
        ArrayList<Integer> newUser = new ArrayList<>();
        for (LocalDate d=begin;!d.isEqual(end.plusDays(1));d=d.plusDays(1)){
            localDates.add(d);
        }

        // 记录前一天的用户总数量
        Integer lastDay=userMapper.countLessThanDate(begin);
        for (LocalDate date : localDates) {
            // 得到每一天的用户总数量，那么今天新增的用户数量=今天用户总量-前一天用户总量
            // 今天的总数量是截至到第二天0点
            Integer today=userMapper.countLessThanDate(date.plusDays(1));
            totalUser.add(today);
            newUser.add(today-lastDay);
            lastDay=today;
        }
        String dateList= StringUtils.join(localDates,",");
        String totalUserList= StringUtils.join(totalUser,",");
        String newUserList= StringUtils.join(newUser,",");

        return UserReportVO.builder()
                .dateList(dateList)
                .totalUserList(totalUserList)
                .newUserList(newUserList).build();
    }

    /**
     * 订单统计接口
     * @param begin
     * @param end
     * @return
     */
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        ArrayList<LocalDate> localDates = new ArrayList<>();
        ArrayList<Integer> orderCount = new ArrayList<>();
        ArrayList<Integer> validOrderCount = new ArrayList<>();
        for (LocalDate d=begin;!d.isEqual(end.plusDays(1));d=d.plusDays(1)){
            localDates.add(d);
        }
        // 总订单数
        Integer totalSum=0;
        // 总有效订单数(状态为已完成)
        Integer validSum=0;
        for (LocalDate date : localDates) {
            // 查询每天的，加一起就是总的 (不用管日期之外的订单)
            Integer tempTotal=orderMapper.countByDateAndStatus(date,date.plusDays(1),null);
            Integer tempValid=orderMapper.countByDateAndStatus(date,date.plusDays(1), Orders.COMPLETED);
            totalSum+=tempTotal;
            orderCount.add(tempTotal);
            validSum+=tempValid;
            validOrderCount.add(tempValid);
        }
        Double orderCompletionRate= validSum.doubleValue()/totalSum;

        String dateList= StringUtils.join(localDates,",");
        String orderCountList= StringUtils.join(orderCount,",");
        String validOrderCountList= StringUtils.join(validOrderCount,",");

        return OrderReportVO.builder()
                .dateList(dateList)
                .orderCountList(orderCountList)
                .validOrderCountList(validOrderCountList)
                .totalOrderCount(totalSum)
                .validOrderCount(validSum)
                .orderCompletionRate(orderCompletionRate).build();
    }

    /**
     * 查询销量排名top10接口
     * @param begin
     * @param end
     * @return
     */
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {

        // 注意sum得到的别名要和实体类中的一致 即number
        List<GoodsSalesDTO> goodsSalesDTOList=orderMapper.top10(begin,end,Orders.COMPLETED);
        List<String> name = goodsSalesDTOList.stream().map(item -> item.getName()).collect(Collectors.toList());
        String nameList=StringUtils.join(name,",");
        List<Integer> number = goodsSalesDTOList.stream().map(item -> item.getNumber()).collect(Collectors.toList());
        String numberList = StringUtils.join(number,",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList).build();
    }

    /**
     * 导出Excel报表接口
     * @param response
     */
    public void export(HttpServletResponse response) {
        // 1.查询数据库获取30天的数据

        // 查询概览数据
        LocalDate begin = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();// 这个其实是今天0点
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);



        // 2.把数据通过POI写入excel文件

        // 获得类对象，再获得类加载器，再从类路径下读取资源(返回输入流对象)
        // 类路径就是编译时的target/classes 根目录   最开头不要加/
        InputStream input = this.getClass().getClassLoader().getResourceAsStream("template/excelTemplate.xlsx");
        try {
            // 读取模板excel文件
            XSSFWorkbook excel = new XSSFWorkbook(input);

            // 获取表格的第一个sheet页
            XSSFSheet sheet = excel.getSheetAt(0);
            // 填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间："+begin+"-"+end);
            // 第四行填写
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            // 第五行填写
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            // 填充明细数据
            for (int i = 0; i < 30; i++) {
                // 最后一天也就是now是今天0点，所以要计算一天的数据，就是从昨天0点到今天0点
                LocalDate date = begin.plusDays(i);
                BusinessDataVO oneday = workspaceService.getBusinessData(date, date.plusDays(1));
                row = sheet.getRow(7+i);
                // 日期
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(oneday.getTurnover());
                row.getCell(3).setCellValue(oneday.getValidOrderCount());
                row.getCell(4).setCellValue(oneday.getOrderCompletionRate());
                row.getCell(5).setCellValue(oneday.getUnitPrice());
                row.getCell(6).setCellValue(oneday.getNewUsers());
            }


            // 3.通过输出流把excel文件下载到浏览器

            // 这里是对象.write(stream)，是因为这里的对象具有写能力，只负责给它通道，它负责数据内容和格式。
            // 而如果是普通的文件，就是先获取文件内容，再用stream.write(内容)，是反过来的
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            // 关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }





    }
}
