package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WorkspaceService workspaceService;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        LocalDateTime beginTime=LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime=LocalDateTime.of(begin, LocalTime.MAX);
        List<Double> turnoverList=new ArrayList<>();
        HashMap map=new HashMap();
        map.put("begin",beginTime);
        map.put("end",endTime);
        map.put("status",Orders.COMPLETED);
        Double cnt=orderMapper.getStaticByDate(map);
        if(cnt==null) cnt=0.0;
        turnoverList.add(cnt);
//        orderMapper.getByStatusOrderTime(Orders.COMPLETED,beginTime);
        while(!begin.equals(end)){
            begin=begin.plusDays(1);
            beginTime=LocalDateTime.of(begin, LocalTime.MIN);
            endTime=LocalDateTime.of(begin, LocalTime.MAX);
            HashMap map1=new HashMap();
            map1.put("begin",beginTime);
            map1.put("end",endTime);
            map1.put("status",Orders.COMPLETED);
            cnt=orderMapper.getStaticByDate(map1);
            if(cnt==null) cnt=0.0;
            turnoverList.add(cnt);
            dateList.add(begin);
        }
//        System.out.println(turnoverList);
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        // 设置为开业前或什么时候的时间
        LocalDateTime firstTime= LocalDateTime.parse("2024-01-01T00:00:00");
        LocalDateTime beginTime=LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime=LocalDateTime.of(begin, LocalTime.MAX);
        List<Integer> totalUserList=new ArrayList<>();
        List<Integer> newUserList=new ArrayList<>();
        newUserList.add(userMapper.userByDate(beginTime,endTime));
        totalUserList.add(userMapper.userByDate(firstTime,endTime));
        while(!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);

            beginTime=LocalDateTime.of(begin, LocalTime.MIN);
            endTime=LocalDateTime.of(begin, LocalTime.MAX);
            newUserList.add(userMapper.userByDate(beginTime,endTime));
            totalUserList.add(userMapper.userByDate(firstTime,endTime));
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        dateList.add(begin);
        // 设置为开业前或什么时候的时间
        LocalDateTime firstTime= LocalDateTime.parse("2024-01-01T00:00:00");
        LocalDateTime beginTime=LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime=LocalDateTime.of(begin, LocalTime.MAX);

        List<Integer> newOrdersList=new ArrayList<>();
        List<Integer> validOrdersNew=new ArrayList<>();

        setOrderInfo(beginTime, endTime, newOrdersList,
                validOrdersNew);

        while(!begin.equals(end)){
            begin=begin.plusDays(1);
            dateList.add(begin);

            beginTime=LocalDateTime.of(begin, LocalTime.MIN);
            endTime=LocalDateTime.of(begin, LocalTime.MAX);

            setOrderInfo(beginTime, endTime, newOrdersList,
                    validOrdersNew);
        }

        Integer totalOrders=orderMapper.getOrder(null,firstTime,endTime);
        Integer validOrders=orderMapper.getOrder(Orders.COMPLETED,firstTime,endTime);
        Double completionRate=(double)validOrders/(double)totalOrders;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalOrderCount(totalOrders)
                .orderCompletionRate(completionRate)
                .validOrderCount(validOrders)
                .orderCountList(StringUtils.join(newOrdersList, ","))
                .validOrderCountList(StringUtils.join(validOrdersNew, ","))
                .build();
    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime=LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime=LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList=orderMapper.getTop10(beginTime,endTime);
        List<String> list = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String name = StringUtils.join(list, ",");
        List<Integer> num = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String number = StringUtils.join(num, ",");
        return SalesTop10ReportVO.builder()
                .nameList(name)
                .numberList(number)
                .build();
    }

    // export excel
    @Override
    public void export(HttpServletResponse response) {
        LocalDate begin=LocalDate.now().minusDays(30);
        LocalDate end=LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(begin, LocalTime.MIN),
                LocalDateTime.of(end, LocalTime.MAX));

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据.xlsx");
        try {
            //基于模板文件创建一个新的Excel文件
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);

            //获取表格文件的Sheet页
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //填充数据--时间
            sheet.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);

            //获得第4行
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            //获得第5行
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());

            //填充明细数据
            for (int i = 0; i < 30; i++) {
                LocalDate date = begin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO dataVO = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                //获得某一行
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(dataVO.getTurnover());
                row.getCell(3).setCellValue(dataVO.getValidOrderCount());
                row.getCell(4).setCellValue(dataVO.getOrderCompletionRate());
                row.getCell(5).setCellValue(dataVO.getUnitPrice());
                row.getCell(6).setCellValue(dataVO.getNewUsers());
            }

            //3. 通过输出流将Excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            //关闭资源
            out.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setOrderInfo(LocalDateTime beginTime, LocalDateTime endTime,
                              List<Integer> newOrdersList, List<Integer> validOrdersNew) {
        newOrdersList.add(orderMapper.getOrder(null,beginTime,endTime));
        validOrdersNew.add(orderMapper.getOrder(Orders.COMPLETED,beginTime,endTime));
        }
}
