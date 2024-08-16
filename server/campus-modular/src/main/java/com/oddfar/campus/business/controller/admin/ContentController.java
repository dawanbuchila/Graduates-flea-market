package com.oddfar.campus.business.controller.admin;

import com.oddfar.campus.business.domain.entity.ContentEntity;
import com.oddfar.campus.business.domain.vo.ContentVo;
import com.oddfar.campus.business.domain.vo.SendContentVo;
import com.oddfar.campus.business.service.ContentService;
import com.oddfar.campus.common.annotation.ApiResource;
import com.oddfar.campus.common.core.page.PageUtils;
import com.oddfar.campus.common.domain.PageResult;
import com.oddfar.campus.common.domain.R;
import com.oddfar.campus.common.enums.ResBizTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * 校园墙内容管理
 *
 * 
 */
@RestController
@RequestMapping("/admin/content")
@ApiResource(name = "内容管理", appCode = "campus", resBizType = ResBizTypeEnum.BUSINESS)
public class ContentController {
    @Autowired
    private ContentService contentService;

    @GetMapping(value = "list", name = "分页")
    
    public R page(ContentEntity contentEntity) {

        //开始分页
        PageUtils.startPage();

        PageResult<ContentVo> page = contentService.page(contentEntity);

        return R.ok().put(page);
    }


    /**
     * 获取校园墙内容详细信息
     */
    
    @GetMapping(value = "/{contentId}", name = "获取校园墙内容")
    public R getInfo(@PathVariable("contentId") Long contentId) {
        ContentEntity contentEntity = new ContentEntity();
        contentEntity.setContentId(contentId);
        return R.ok(contentService.selectContentByContentId(contentEntity));
    }

    /**
     * 添加校园墙
     */
    @PostMapping(value = "", name = "添加信息墙内容")
    public R add(@Validated @RequestBody ContentEntity content) {

        SendContentVo sendContentVo = new SendContentVo();
        sendContentVo.setContent(content.getContent());
        sendContentVo.setType(content.getType());
        sendContentVo.setCategoryId(content.getCategoryId());
        sendContentVo.setIsAnonymous(content.getIsAnonymous());
        sendContentVo.setFileList(new ArrayList<>());


        return R.ok(contentService.sendContent(sendContentVo));
    }


    /**
     * 修改校园墙内容
     */
    
    @PutMapping(value = "", name = "修改信息墙内容")
    public R edit(@Validated @RequestBody ContentEntity content) {
        return R.ok(contentService.updateContent(content));
    }

    /**
     * 删除校园墙内容
     */
    
    @DeleteMapping("/{contentIds}")
    public R remove(@PathVariable Long[] contentIds) {
        for (Long contentId : contentIds) {
            contentService.deleteOwnContent(contentId);
        }
        return R.ok();
    }


}
