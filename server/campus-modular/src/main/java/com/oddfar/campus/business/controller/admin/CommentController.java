package com.oddfar.campus.business.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oddfar.campus.business.domain.entity.CommentEntity;
import com.oddfar.campus.business.service.CommentService;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/admin/comment")
@ApiResource(name = "评论管理", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class CommentController {

    @Autowired
    private CommentService commentService;



    /**
     * 查询评论列表
     */
    
    @GetMapping(value = "/list")
    public R list(CommentEntity comment) {
        PageResult<CommentEntity> page = commentService.page(comment);

        return R.ok().put(page);
    }


    /**
     * 获取评论详细信息
     */
    
    @GetMapping(value = "/{commentId}")
    public R getInfo(@PathVariable("commentId") Long commentId) {
        return R.ok(commentService.getById(commentId));
    }

    /**
     * 新增评论
     */
    
    @PostMapping
    public R add(@Validated @RequestBody CommentEntity comment) {
        return R.ok(commentService.insertComment(comment));
    }

    /**
     * 修改评论
     */
    
    @PutMapping
    public R edit(@Validated @RequestBody CommentEntity comment) {
        return R.ok(commentService.updateComment(comment));
    }

    /**
     * 删除评论
     */
    
    @DeleteMapping("/{commentIds}")
    public R remove(@PathVariable Long[] commentIds) {
        return R.ok(commentService.removeBatchByIds(Arrays.asList(commentIds)));
    }

    /**
     * 导出评论
     */
    
    @PostMapping("/export")
    public void export(HttpServletResponse  response) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Comments");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("评论ID");
            headerRow.createCell(1).setCellValue("上级评论ID");
            headerRow.createCell(2).setCellValue("用户ID");
            headerRow.createCell(3).setCellValue("目标用户ID");
            headerRow.createCell(4).setCellValue("一级评论ID");
            headerRow.createCell(5).setCellValue("内容ID");
            headerRow.createCell(6).setCellValue("评论内容");
            headerRow.createCell(7).setCellValue("IP");
            headerRow.createCell(8).setCellValue("地址");

            // 获取评论数据
            List<CommentEntity> comments = commentService.list();

            int rowNum = 1;
            for (CommentEntity comment : comments) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(comment.getCommentId());
                row.createCell(1).setCellValue(comment.getParentId());
                row.createCell(2).setCellValue(comment.getUserId());
                row.createCell(3).setCellValue(comment.getToUserId());
                row.createCell(4).setCellValue(comment.getOneLevelId());
                row.createCell(5).setCellValue(comment.getContentId());
                row.createCell(6).setCellValue(comment.getCoContent());
                row.createCell(7).setCellValue(comment.getIp());
                row.createCell(8).setCellValue(comment.getAddress());

                // 设置其他单元格的值
            }

            // 设置响应头，告诉浏览器返回的是一个Excel文件
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=comments.xlsx");

            // 将工作簿写入响应流中
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            // 处理导出异常情况
        }



    }







}
