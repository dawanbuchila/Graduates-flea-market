package com.oddfar.campus.business.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oddfar.campus.business.domain.entity.CategoryEntity;
import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.vo.DateAddUserVo;
import com.oddfar.campus.business.domain.vo.StaticDataVo;
import com.oddfar.campus.business.service.CategoryService;
import com.oddfar.campus.business.service.ContentService;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.domain.entity.SysUserEntity;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import com.oddfar.campus.framework.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/admin/static")
@ApiResource(name = "内容管理", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class IndexController {

    @Autowired
    private SysUserService userService;

    @Autowired
    private ContentService contentService;


    @Autowired
    private CategoryService categoryService;

    // 新增代码： 获取日期添加用户数据
    @GetMapping("/dateUser/{startTime}/{endTime}")  // 时间格式2024-02-26
    public R getDateUser(@PathVariable String startTime, @PathVariable String endTime) {
        Date start = parseDate(startTime);
        Date end = parseDate(endTime);

        PageResult<SysUserEntity> page = userService.page(new SysUserEntity());

        List<SysUserEntity> allUser = page.getRows();

        // 单独获取每一天新增的用户数

        DateAddUserVo result = calculateAddUserByDate(start, end, allUser);


        return R.ok(result);
    }

    // 新增代码：获取所有首页信息
    @GetMapping("/data")
    public R getStaticData() {

        StaticDataVo result = new StaticDataVo();


        // 获取总人数
        PageResult<SysUserEntity> page = userService.page(new SysUserEntity());
        result.setUserNum((int) page.getTotal());

        // 获取总分类数量
        result.setCategoryNum(this.categoryService.list(new LambdaQueryWrapper<CategoryEntity>()
                .ne(CategoryEntity::getParentId, 0)).size());

        // 今日新增帖子数
        LocalDate now = LocalDate.now();
        LocalDateTime startOfDay = LocalDateTime.of(now, LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(now, LocalTime.MAX);

        List<ContentEntity> todayContents = this.contentService.list(new LambdaQueryWrapper<ContentEntity>()
                .ge(ContentEntity::getCreateTime, startOfDay)
                .lt(ContentEntity::getCreateTime, endOfDay));

        result.setAddContentNum(todayContents.size());

        // 检查在线人数
        LocalDate today = LocalDate.now();
        List<SysUserEntity> allUser = page.getRows();
        List<SysUserEntity> todayLoggedInUsers = new ArrayList<>();
        for (SysUserEntity user : allUser) {
            LocalDate updateDate = user.getUpdateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (updateDate.isEqual(today)) {
                todayLoggedInUsers.add(user);
            }
        }

        result.setOnlineNum(todayLoggedInUsers.size());


        // 封装柱状图
        ArrayList<CategoryEntity> fatherCategoryName = (ArrayList<CategoryEntity>) this.categoryService.list(new LambdaQueryWrapper<CategoryEntity>()
                .eq(CategoryEntity::getParentId, 0));

        ArrayList<String> name = new ArrayList<>();
        ArrayList<Integer> num = new ArrayList<>();


        for (CategoryEntity item : fatherCategoryName) {

            // 获取子分类
            List<CategoryEntity> sunList = this.categoryService.list(new LambdaQueryWrapper<CategoryEntity>()
                    .eq(CategoryEntity::getParentId, item.getCategoryId()));

            if (sunList.size() != 0) {
                // 获取子分类所有帖子
                for (CategoryEntity categoryEntity : sunList) {

                    // 根据根类
                    List<ContentEntity> list = this.contentService.list(new LambdaQueryWrapper<ContentEntity>()
                            .eq(ContentEntity::getCategoryId, categoryEntity.getCategoryId()));



                    name.add(categoryEntity.getCategoryName());
                    num.add(list.size());

                }


            }




        }

        result.setCategoryNameList(name);
        result.setCategoryNumList(num);

        return R.ok(result);

    }

    public static Date parseDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(dateString);
        } catch (ParseException e) {
            // 处理异常情况
            e.printStackTrace();
            return null;
        }
    }

    private DateAddUserVo calculateAddUserByDate(Date start, Date end, List<SysUserEntity> users) {
        DateAddUserVo result = new DateAddUserVo();
        ArrayList<String> addDate = new ArrayList<>();
        ArrayList<Integer> addNum = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        while (!calendar.getTime().after(end)) {
            int count = 0;
            for (SysUserEntity user : users) {
                if (isSameDay(calendar.getTime(), user.getCreateTime())) {
                    count++;
                }
            }
            addDate.add(formatDate(calendar.getTime()));
            addNum.add(count);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        result.setAddDate(addDate);
        result.setAddNum(addNum);

        return result;
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }



}
