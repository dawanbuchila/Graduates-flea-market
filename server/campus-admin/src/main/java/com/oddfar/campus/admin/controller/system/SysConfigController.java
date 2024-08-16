package com.oddfar.campus.admin.controller.system;

import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.domain.entity.SysConfigEntity;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import com.oddfar.campus.framework.service.SysConfigService;
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

/**
 * 配置管理
 */
@RestController
@RequestMapping("/system/config")
@ApiResource(name = "参数配置管理", resBizType = ResBizTypeEnum.SYSTEM)
public class SysConfigController {
    @Autowired
    private SysConfigService configService;

    @GetMapping(value = "page", name = "参数配置管理-分页")
    @PreAuthorize("@ss.hasPermi('system:config:list')")
    public R page(SysConfigEntity sysConfigEntity) {
        PageResult<SysConfigEntity> page = configService.page(sysConfigEntity);

        return R.ok().put(page);
    }

    @GetMapping(value = "{id}", name = "参数配置管理-查询id信息")
    @PreAuthorize("@ss.hasPermi('system:config:query')")
    public R getInfo(@PathVariable("id") Long id) {
        SysConfigEntity entity = configService.selectConfigById(id);

        return R.ok().put(entity);
    }

    /**
     * 根据参数键名查询参数值
     */
    @GetMapping(value = "/configKey/{configKey:.+}")
    public R getConfigKey(@PathVariable String configKey) {
        return R.ok(configService.selectConfigByKey(configKey));
    }

    @PostMapping(name = "参数配置管理-新增")
    @PreAuthorize("@ss.hasPermi('system:config:add')")
    public R add(@Validated @RequestBody SysConfigEntity config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return R.error("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        return R.ok(configService.insertConfig(config));

    }

    @PutMapping(name = "参数配置管理-修改")
    @PreAuthorize("@ss.hasPermi('system:config:edit')")
    public R edit(@Validated @RequestBody SysConfigEntity config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return R.error("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        return R.ok(configService.updateConfig(config));

    }

    @DeleteMapping(value = "/{configIds}", name = "参数配置管理-删除")
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    public R remove(@PathVariable Long[] configIds) {
        configService.deleteConfigByIds(configIds);

        return R.ok();
    }

    /**
     * 刷新参数缓存
     */
    @PreAuthorize("@ss.hasPermi('system:config:remove')")
    @DeleteMapping(value = "/refreshCache", name = "参数配置管理-刷新缓存")
    public R refreshCache() {
        configService.resetConfigCache();
        return R.ok();
    }



    /**
     * 导出config
     */
    
    @PostMapping("/export")
    public void export(HttpServletResponse response) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("SysConfig");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("参数名称");
            headerRow.createCell(1).setCellValue("参数键名");
            headerRow.createCell(2).setCellValue("参数键值");
            headerRow.createCell(3).setCellValue("系统内置");
            headerRow.createCell(4).setCellValue("所属分类的编码");
            headerRow.createCell(5).setCellValue("备注");

            // 假设你使用configService从数据库中获取配置数据
            SysConfigEntity sysConfigEntity = new SysConfigEntity();
            sysConfigEntity.setPageNum(1);
            sysConfigEntity.setPageSize(9999);
            List<SysConfigEntity> configList = configService.page(sysConfigEntity).getRows();

            int rowNum = 1;
            for (SysConfigEntity config : configList) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(config.getConfigName());
                row.createCell(1).setCellValue(config.getConfigKey());
                row.createCell(2).setCellValue(config.getConfigValue());
                row.createCell(3).setCellValue(config.getConfigType());
                row.createCell(4).setCellValue(config.getGroupCode());
                row.createCell(5).setCellValue(config.getRemark());

                // 设置其他单元格的值
            }

            // 设置响应头，告诉浏览器返回的是一个 Excel 文件
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=sysConfig.xlsx");

            // 将工作簿写入响应流中
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            // 处理导出异常情况
        }





    }
}