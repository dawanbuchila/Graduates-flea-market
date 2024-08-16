package com.oddfar.campus.admin.controller.system;

import com.oddfar.campus.common.annotation.Anonymous;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.domain.entity.SysDictDataEntity;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import com.oddfar.campus.framework.service.SysDictDataService;
import com.oddfar.campus.framework.service.SysDictTypeService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/system/dict/data")
@ApiResource(name = "字典数据管理", resBizType = ResBizTypeEnum.SYSTEM)
public class SysDictDataController {
    @Autowired
    private SysDictDataService dictDataService;
    @Autowired
    private SysDictTypeService dictTypeService;


    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @GetMapping(value = "/list", name = "字典数据管理-分页")
    public R page(SysDictDataEntity dictData) {
        PageResult<SysDictDataEntity> page = dictDataService.page(dictData);
        return R.ok().put(page);
    }

    /**
     * 根据字典类型查询字典数据信息
     */
    @GetMapping(value = "/type/{dictType}", name = "字典数据管理-根据字典类型查询字典数据信息")
    @Anonymous
    public R dictType(@PathVariable String dictType) {

        List<SysDictDataEntity> data = dictTypeService.selectDictDataByType(dictType);
        if (StringUtils.isEmpty(data)) {
            data = new ArrayList<SysDictDataEntity>();
        }
        return R.ok().put(data);
    }


    /**
     * 查询字典数据详细
     */
    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @GetMapping(value = "/{dictCode}", name = "字典数据管理-查询")
    public R getInfo(@PathVariable Long dictCode) {
        return R.ok(dictDataService.selectDictDataById(dictCode));
    }


    /**
     * 新增字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @PostMapping(name = "字典数据管理-新增")
    public R add(@Validated @RequestBody SysDictDataEntity dict) {
        return R.ok(dictDataService.insertDictData(dict));
    }

    /**
     * 修改保存字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:edit')")
    @PutMapping(name = "字典数据管理-修改")
    public R edit(@Validated @RequestBody SysDictDataEntity dict) {
        return R.ok(dictDataService.updateDictData(dict));
    }

    /**
     * 删除字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @DeleteMapping(value = "/{dictCodes}", name = "字典数据管理-删除")
    public R remove(@PathVariable Long[] dictCodes) {
        dictDataService.deleteDictDataByIds(dictCodes);
        return R.ok();
    }



    /**
     * 导出字典
     */
    
    @PostMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DictData");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("字典编码");
            headerRow.createCell(1).setCellValue("字典排序");
            headerRow.createCell(2).setCellValue("字典标签");
            headerRow.createCell(3).setCellValue("字典键值");
            headerRow.createCell(4).setCellValue("字典类型");
            headerRow.createCell(5).setCellValue("样式属性");
            headerRow.createCell(6).setCellValue("表格字典样式");
            headerRow.createCell(7).setCellValue("是否默认");
            headerRow.createCell(8).setCellValue("状态");
            headerRow.createCell(9).setCellValue("备注");

            // 假设你使用dictDataService从数据库中获取字典数据
            SysDictDataEntity s = new SysDictDataEntity();
            s.setPageNum(1);
            s.setPageSize(2000);
            List<SysDictDataEntity> dictDataList = dictDataService.page(s).getRows();

            int rowNum = 1;
            for (SysDictDataEntity dictData : dictDataList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(dictData.getDictCode());
                row.createCell(1).setCellValue(dictData.getDictSort());
                row.createCell(2).setCellValue(dictData.getDictLabel());
                row.createCell(3).setCellValue(dictData.getDictValue());
                row.createCell(4).setCellValue(dictData.getDictType());
                row.createCell(5).setCellValue(dictData.getCssClass());
                row.createCell(6).setCellValue(dictData.getListClass());
                row.createCell(7).setCellValue(dictData.getDefault() ? "是" : "否");
                row.createCell(8).setCellValue(dictData.getStatus());
                row.createCell(9).setCellValue(dictData.getRemark());

                // 设置其他单元格的值
            }

            // 设置响应头，告诉浏览器返回的是一个 Excel 文件
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=dictData.xlsx");

            // 将工作簿写入响应流中
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            // 处理导出异常情况
        }
    }







}
