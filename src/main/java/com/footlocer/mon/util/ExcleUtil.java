package com.footlocer.mon.util;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.footlocer.mon.dto.StockInfo;
import com.footlocer.mon.entity.ShoeExcle;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

public class ExcleUtil {


    public static void exportFootlocer(List<StockInfo> StockInfoList, HttpServletResponse response) throws IOException {
        //在内存操作，写到浏览器
        ExcelWriter writer= ExcelUtil.getWriter(true);

        //自定义标题别名
        writer.addHeaderAlias("sku","货号");
        writer.addHeaderAlias("footlockerSize","flus");
        writer.addHeaderAlias("champssportSize","csus");
        writer.addHeaderAlias("kidSize","kid");


        //默认配置
        writer.write(StockInfoList,true);
        //设置content—type
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset:utf-8");

        //设置标题
        String fileName= URLEncoder.encode("用户信息","UTF-8");
        //Content-disposition是MIME协议的扩展，MIME协议指示MIME用户代理如何显示附加的文件。
        response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");
        ServletOutputStream outputStream= response.getOutputStream();

        //将Writer刷新到OutPut
        writer.flush(outputStream,true);
        outputStream.close();
        writer.close();

    }


    public static void exportTouch(List<ShoeExcle> shoeExcleList, HttpServletResponse response) throws IOException {
        //在内存操作，写到浏览器
        ExcelWriter writer= ExcelUtil.getWriter(true);

        //自定义标题别名
        writer.addHeaderAlias("sku","货号");
        writer.addHeaderAlias("stockxHandPrice","stockx到手价");
        writer.addHeaderAlias("dewuHandPrice","得物到手价");
        writer.addHeaderAlias("saleAmount","stockx三日销量");

        //默认配置
        writer.write(shoeExcleList,true);
        //设置content—type
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset:utf-8");

        //设置标题
        String fileName= URLEncoder.encode("用户信息","UTF-8");
        //Content-disposition是MIME协议的扩展，MIME协议指示MIME用户代理如何显示附加的文件。
        response.setHeader("Content-Disposition","attachment;filename="+fileName+".xlsx");
        ServletOutputStream outputStream= response.getOutputStream();

        //将Writer刷新到OutPut
        writer.flush(outputStream,true);
        outputStream.close();
        writer.close();

    }

}
