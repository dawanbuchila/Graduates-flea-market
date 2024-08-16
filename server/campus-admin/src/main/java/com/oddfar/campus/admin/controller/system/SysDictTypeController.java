package com.oddfar.campus.admin.controller.system;

import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.domain.entity.SysDictDataEntity;
import com.oddfar.campus.common.domain.entity.SysDictTypeEntity;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import com.oddfar.campus.framework.service.SysDictTypeService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/system/dict/type")
@ApiResource(name = "字典类型管理", resBizType = ResBizTypeEnum.SYSTEM)
public class SysDictTypeController {

    @Autowired
    private SysDictTypeService dictTypeService;

    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @GetMapping(value = "/list", name = "字典类型管理-分页")
    public R list(SysDictTypeEntity sysDictTypeEntity) {
        PageResult<SysDictTypeEntity> page = dictTypeService.page(sysDictTypeEntity);
        return R.ok().put(page);
    }

    /**
     * 查询字典类型详细
     */
    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @GetMapping(value = "/{dictId}", name = "字典类型管理-查询")
    public R getInfo(@PathVariable Long dictId) {
        return R.ok(dictTypeService.selectDictTypeById(dictId));
    }

    /**
     * 新增字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @PostMapping(name = "字典类型管理-新增")
    public R add(@Validated @RequestBody SysDictTypeEntity dict) {
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            return R.error("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }

        return R.ok(dictTypeService.insertDictType(dict));
    }

    /**
     * 修改字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:edit')")
    @PutMapping(name = "字典类型管理-修改")
    public R edit(@Validated @RequestBody SysDictTypeEntity dict) {
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            return R.error("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        return R.ok(dictTypeService.updateDictType(dict));
    }

    /**
     * 删除字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @DeleteMapping(value = "/{dictIds}", name = "字典类型管理-删除")
    public R remove(@PathVariable Long[] dictIds) {
        dictTypeService.deleteDictTypeByIds(dictIds);
        return R.ok();
    }

    /**
     * 刷新字典缓存
     */
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @DeleteMapping(value = "/refreshCache", name = "字典类型管理-刷新")
    public R refreshCache() {
        dictTypeService.resetDictCache();
        return R.ok();
    }

    /**
     * 获取字典选择框列表
     */
    @GetMapping(value = "/optionselect", name = "字典类型管理-获取字典选择框列表")
    public R optionselect() {
        List<SysDictTypeEntity> dictTypes = dictTypeService.selectDictTypeAll();
        return R.ok(dictTypes);
    }


    /**
     * 导出字典类型
     */
    
    @PostMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("SysDictType");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("字典主键");
            headerRow.createCell(1).setCellValue("字典名称");
            headerRow.createCell(2).setCellValue("字典类型");
            headerRow.createCell(3).setCellValue("状态");
            headerRow.createCell(4).setCellValue("备注");

            // 假设你使用dictTypeService从数据库中获取字典类型数据
            SysDictTypeEntity d = new SysDictTypeEntity();
            d.setPageNum(1);
            d.setPageSize(9999);
            List<SysDictTypeEntity> dictTypeList = dictTypeService.page(d).getRows();

            int rowNum = 1;
            for (SysDictTypeEntity dictType : dictTypeList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(dictType.getDictId());
                row.createCell(1).setCellValue(dictType.getDictName());
                row.createCell(2).setCellValue(dictType.getDictType());
                row.createCell(3).setCellValue(dictType.getStatus());
                row.createCell(4).setCellValue(dictType.getRemark());

                // 设置其他单元格的值
            }

            // 设置响应头，告诉浏览器返回的是一个 Excel 文件
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=sysDictType.xlsx");

            // 将工作簿写入响应流中
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            // 处理导出异常情况
        }
    }

}
