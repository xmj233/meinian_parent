package com.atguigu.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.constant.MessageConstant;
import com.atguigu.entity.Result;
import com.atguigu.service.MemberService;
import com.atguigu.service.ReportService;
import com.atguigu.service.SetMealService;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.aspectj.bridge.Message;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/report")
public class ReportController {
    @Reference
    MemberService memberService;
    @Reference
    SetMealService setMealService;
    @Reference
    ReportService reportService;

    @RequestMapping("/getMemberReport")
    public Result getMemberReport() {
        try {
            Map<String, Object> map = new HashMap<>();
            List<String> months = new ArrayList<>();
            List<Integer> memberCount = new ArrayList<>();

            Calendar calendar = Calendar.getInstance();
            calendar.add(calendar.MONTH, -12);
            for (int i = 0; i < 12; i++) {
                calendar.add(calendar.MONTH, 1);
                months.add(new SimpleDateFormat("yyyy-MM").format(calendar.getTime()));
            }
            map.put("months", months);

            memberCount = memberService.findMemberCountByMonth(months);
            map.put("memberCount", memberCount);
            return new Result(true, MessageConstant.GET_MEMBER_NUMBER_REPORT_SUCCESS, map);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, MessageConstant.GET_MEMBER_NUMBER_REPORT_FAIL);
        }
    }

    @RequestMapping("/getSetmealReport")
    public Result getSetmealReport() {
        try {
            HashMap<String, Object> map = new HashMap<>();
            List<String> setmealNames = new ArrayList<>();
            List<Map<String, Object>> setmealCount = new ArrayList<>();

            setmealCount = setMealService.findSetmealCount();

            for (Map<String, Object> m : setmealCount) {
                String name = (String) m.get("name");
                setmealNames.add(name);
            }
            map.put("setmealCount", setmealCount);
            map.put("setmealNames", setmealNames);

            return new Result(true, MessageConstant.GET_SETMEAL_COUNT_REPORT_SUCCESS, map);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, MessageConstant.GET_SETMEAL_COUNT_REPORT_FAIL);
        }
    }

    @RequestMapping("/getBusinessReportData")
    public Result getBusinessReportData() {
        try {
            Map<String, Object> map = new HashMap<>();
            map = reportService.getBusinessReportData();
            return new Result(true, MessageConstant.GET_BUSINESS_REPORT_SUCCESS, map);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, MessageConstant.GET_BUSINESS_REPORT_FAIL);
        }
    }

    @RequestMapping("/exportBusinessReport")
    public Result exportBusinessReport(HttpServletRequest request, HttpServletResponse response){
        try{
            //??????????????????????????????????????????
            Map<String, Object> result = reportService.getBusinessReportData();

            //?????????????????????????????????????????????????????????Excel?????????
            String reportDate = (String) result.get("reportDate");
            Integer todayNewMember = (Integer) result.get("todayNewMember");
            Integer totalMember = (Integer) result.get("totalMember");
            Integer thisWeekNewMember = (Integer) result.get("thisWeekNewMember");
            Integer thisMonthNewMember = (Integer) result.get("thisMonthNewMember");
            Integer todayOrderNumber = (Integer) result.get("todayOrderNumber");
            Integer thisWeekOrderNumber = (Integer) result.get("thisWeekOrderNumber");
            Integer thisMonthOrderNumber = (Integer) result.get("thisMonthOrderNumber");
            Integer todayVisitsNumber = (Integer) result.get("todayVisitsNumber");
            Integer thisWeekVisitsNumber = (Integer) result.get("thisWeekVisitsNumber");
            Integer thisMonthVisitsNumber = (Integer) result.get("thisMonthVisitsNumber");
            List<Map> hotSetmeal = (List<Map>) result.get("hotSetmeal");

            //??????Excel????????????????????????
            //file.separator??????????????????????????????????????????????????????????????????
            String temlateRealPath = request.getSession().getServletContext().getRealPath("template") +
                    File.separator + "report_template.xlsx";

            //????????????????????????Excel????????????
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(new File(temlateRealPath)));
            XSSFSheet sheet = workbook.getSheetAt(0);

            XSSFRow row = sheet.getRow(2);
            row.getCell(5).setCellValue(reportDate);//??????

            row = sheet.getRow(4);
            row.getCell(5).setCellValue(todayNewMember);//???????????????????????????
            row.getCell(7).setCellValue(totalMember);//????????????

            row = sheet.getRow(5);
            row.getCell(5).setCellValue(thisWeekNewMember);//?????????????????????
            row.getCell(7).setCellValue(thisMonthNewMember);//?????????????????????

            row = sheet.getRow(7);
            row.getCell(5).setCellValue(todayOrderNumber);//???????????????
            row.getCell(7).setCellValue(todayVisitsNumber);//???????????????

            row = sheet.getRow(8);
            row.getCell(5).setCellValue(thisWeekOrderNumber);//???????????????
            row.getCell(7).setCellValue(thisWeekVisitsNumber);//???????????????

            row = sheet.getRow(9);
            row.getCell(5).setCellValue(thisMonthOrderNumber);//???????????????
            row.getCell(7).setCellValue(thisMonthVisitsNumber);//???????????????

            int rowNum = 12;
            for(Map map : hotSetmeal){//????????????
                String name = (String) map.get("name");
                Long setmeal_count = (Long) map.get("setmeal_count");
                BigDecimal proportion = (BigDecimal) map.get("proportion");
                row = sheet.getRow(rowNum ++);
                row.getCell(4).setCellValue(name);//????????????
                row.getCell(5).setCellValue(setmeal_count);//????????????
                row.getCell(6).setCellValue(proportion.doubleValue());//??????
            }

            //?????????????????????????????????
            ServletOutputStream out = response.getOutputStream();
            // ????????????????????????excel?????????
            response.setContentType("application/vnd.ms-excel");
            // ??????????????????(???????????????????????????)
            response.setHeader("content-Disposition", "attachment;filename=report.xlsx");
            workbook.write(out);

            out.flush();
            out.close();
            workbook.close();

            return null;
        }catch (Exception e){
            return new Result(false, MessageConstant.GET_BUSINESS_REPORT_FAIL,null);
        }
    }
}
