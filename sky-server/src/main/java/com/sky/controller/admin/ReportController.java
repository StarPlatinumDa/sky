package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 数据统计相关接口
 */
@RestController
@RequestMapping("/admin/report")
@Api(tags = "数据统计相关接口")
@Slf4j
public class ReportController {
    @Autowired
    private ReportService reportService;

    @GetMapping("/turnoverStatistics")
    @ApiOperation("营业额统计接口")
    public Result turnoverStatistics(
            // 前端传参时为了保证时间能被正确解读，需要注释
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){

        log.info("营业额统计,{},{}",begin,end);
        TurnoverReportVO res=reportService.turnoverStatistics(begin,end);

        return Result.success(res);
    }

    /**
     * 用户数量统计接口
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/userStatistics")
    @ApiOperation("用户数量统计接口")
    public Result userStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("用户数量统计,{},{}",begin,end);
        UserReportVO userReportVO=reportService.userStatistics(begin,end);
        return Result.success(userReportVO);
    }

    /**
     * 订单统计接口
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计接口")
    public Result ordersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("订单统计接口,{},{}",begin,end);
        OrderReportVO orderReportVO=reportService.ordersStatistics(begin,end);
        return Result.success(orderReportVO);
    }


    /**
     * 查询销量排名top10接口
     * @param begin
     * @param end
     * @return
     */
    @GetMapping("/top10")
    @ApiOperation("查询销量排名top10接口")
    public Result top10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end){
        log.info("订查询销量排名top10接口,{},{}",begin,end);
        SalesTop10ReportVO salesTop10ReportVO=reportService.top10(begin,end);
        return Result.success(salesTop10ReportVO);
    }

    /**
     * 导出Excel报表接口
     * @param response
     */
    @GetMapping("/export")
    @ApiOperation("导出Excel报表接口")
    // response对象用于把文件写给浏览器，即下载
    // 普通的接口 框架其实自动调用了response把对象返回 response.getWriter().write(...) 并且默认Content-Type: application/json
    // 也可在GetMapping里设置@GetMapping(value = "/xml", produces = "application/octet-stream")  二进制

    // 而二进制文件需要自己手动写，自己写ContentType  Header
    public void export(HttpServletResponse response){
        log.info("导出Excel报表接口");
        reportService.export(response);
    }
}
