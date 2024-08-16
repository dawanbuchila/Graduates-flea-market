package com.oddfar.campus.admin.controller.system;

import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.core.page.PageUtils;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.domain.entity.SysRoleEntity;
import com.oddfar.campus.common.domain.entity.SysUserEntity;
import com.oddfar.campus.common.domain.entity.SysUserRoleEntity;
import com.oddfar.campus.common.domain.model.LoginUser;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import com.oddfar.campus.common.utils.SecurityUtils;
import com.oddfar.campus.common.utils.StringUtils;
import com.oddfar.campus.framework.service.SysRoleService;
import com.oddfar.campus.framework.service.SysUserService;
import com.oddfar.campus.framework.web.service.SysPermissionService;
import com.oddfar.campus.framework.web.service.TokenService;
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
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/system/role")
@ApiResource(name = "角色管理" , resBizType = ResBizTypeEnum.SYSTEM)
public class SysRoleController {

    @Autowired
    private SysRoleService roleService;
    @Autowired
    private SysUserService userService;
    @Autowired
    private SysPermissionService permissionService;
    @Autowired
    private TokenService tokenService;

    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/list")
    public R list(SysRoleEntity role) {
        PageResult<SysRoleEntity> list = roleService.page(role);
        return R.ok().put(list);
    }

    /**
     * 根据角色编号获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:role:query')")
    @GetMapping(value = "/{roleId}")
    public R getInfo(@PathVariable Long roleId) {
        return R.ok(roleService.selectRoleById(roleId));
    }

    /**
     * 新增角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:add')")
    @PostMapping
    public R add(@Validated @RequestBody SysRoleEntity role) {
        if (!roleService.checkRoleNameUnique(role)) {
            return R.error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(role)) {
            return R.error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        return R.ok(roleService.insertRole(role));

    }

    /**
     * 修改保存角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping
    public R edit(@Validated @RequestBody SysRoleEntity role) {
        roleService.checkRoleAllowed(role);
        if (!roleService.checkRoleNameUnique(role)) {
            return R.error("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(role)) {
            return R.error("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }

        if (roleService.updateRole(role) > 0) {
            // 更新缓存用户权限
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (StringUtils.isNotNull(loginUser.getUser()) && !loginUser.getUser().isAdmin()) {
                loginUser.setPermissions(permissionService.getMenuPermission(loginUser.getUser()));
                loginUser.setUser(userService.selectUserByUserName(loginUser.getUser().getUserName()));
                tokenService.setLoginUser(loginUser);
            }
            permissionService.resetLoginUserRoleCache(role.getRoleId());
            return R.ok();
        }
        return R.error("修改角色'" + role.getRoleName() + "'失败，请联系管理员");
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping("/changeStatus")
    public R changeStatus(@RequestBody SysRoleEntity role) {
        roleService.checkRoleAllowed(role);
        roleService.updateRoleStatus(role);
        //更新redis缓存权限数据
        permissionService.resetLoginUserRoleCache(role.getRoleId());
        return R.ok();
    }

    /**
     * 删除角色
     */
    @PreAuthorize("@ss.hasPermi('system:role:remove')")
    @DeleteMapping("/{roleIds}")
    public R remove(@PathVariable Long[] roleIds) {
        roleService.deleteRoleByIds(roleIds);
        //更新redis缓存权限数据
        Arrays.stream(roleIds).forEach(id -> permissionService.resetLoginUserRoleCache(id));

        return R.ok();
    }


    /**
     * 查询已分配用户角色列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/allocatedList")
    public R allocatedList(SysUserEntity user) {
        PageUtils.startPage();
        List<SysUserEntity> list = userService.selectAllocatedList(user);
        return R.ok().put(PageUtils.getPageResult(list));
    }

    /**
     * 查询未分配用户角色列表
     */
    @PreAuthorize("@ss.hasPermi('system:role:list')")
    @GetMapping("/authUser/unallocatedList")
    public R unallocatedList(SysUserEntity user) {
        PageUtils.startPage();
        List<SysUserEntity> list = userService.selectUnallocatedList(user);
        return R.ok().put(PageUtils.getPageResult(list));
    }

    /**
     * 取消授权用户
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping("/authUser/cancel")
    public R cancelAuthUser(@RequestBody SysUserRoleEntity userRole) {
        int i = roleService.deleteAuthUser(userRole);
        //更新redis缓存权限数据
        permissionService.resetLoginUserRoleCache(userRole.getRoleId());
        return R.ok(i);
    }

    /**
     * 批量取消授权用户
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping("/authUser/cancelAll")
    public R cancelAuthUserAll(Long roleId, Long[] userIds) {
        int i = roleService.deleteAuthUsers(roleId, userIds);
        //更新redis缓存权限数据
        permissionService.resetLoginUserRoleCache(roleId);

        return R.ok(i);
    }

    /**
     * 批量选择用户授权
     */
    @PreAuthorize("@ss.hasPermi('system:role:edit')")
    @PutMapping("/authUser/selectAll")
    public R selectAuthUserAll(Long roleId, Long[] userIds) {
        return R.ok(roleService.insertAuthUsers(roleId, userIds));
    }



    /**
     * 导出角色
     */
    
    @PostMapping("/export")
    public void export(HttpServletResponse response) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Roles");

            // 创建表头
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("角色ID");
            headerRow.createCell(1).setCellValue("角色名称");
            headerRow.createCell(2).setCellValue("角色权限");
            headerRow.createCell(3).setCellValue("角色排序");
            headerRow.createCell(4).setCellValue("菜单树选择项是否关联显示");
            headerRow.createCell(5).setCellValue("角色状态");
            headerRow.createCell(6).setCellValue("备注");

            // 假设你使用roleService从数据库中获取角色数据
            SysRoleEntity ro = new SysRoleEntity();
            ro.setPageNum(1);
            ro.setPageSize(2000);
            List<SysRoleEntity> roles = roleService.page(ro).getRows();

            int rowNum = 1;
            for (SysRoleEntity role : roles) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(role.getRoleId());
                row.createCell(1).setCellValue(role.getRoleName());
                row.createCell(2).setCellValue(role.getRoleKey());
                row.createCell(3).setCellValue(role.getRoleSort());
                row.createCell(4).setCellValue(role.isMenuCheckStrictly() ? "是" : "否");
                row.createCell(5).setCellValue(role.getStatus());
                row.createCell(6).setCellValue(role.getRemark());

                // 设置其他单元格的值
            }

            // 设置响应头，告诉浏览器返回的是一个 Excel 文件
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=roles.xlsx");

            // 将工作簿写入响应流中
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            // 处理导出异常情况
        }





    }




}
