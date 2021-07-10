package com.freedy.mall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.freedy.mall.product.entity.AttrAttrgroupRelationEntity;
import com.freedy.mall.product.entity.AttrEntity;
import com.freedy.mall.product.service.AttrAttrgroupRelationService;
import com.freedy.mall.product.service.AttrService;
import com.freedy.mall.product.service.CategoryService;
import com.freedy.mall.product.vo.AttrGroupRelationVo;
import com.freedy.mall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.freedy.mall.product.entity.AttrGroupEntity;
import com.freedy.mall.product.service.AttrGroupService;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.R;


/**
 * 属性分组
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 15:44:51
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    ///product/attrgroup/{catelogId}225/withattr
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catId) {
        //查出当前分类下的所有属性分组
        List<AttrGroupEntity> attrGroups = attrGroupService.getAttrGroupById(catId);
        //查出每个属性分组的所有属性
        List<AttrGroupWithAttrsVo> res = null;
        if (attrGroups != null && attrGroups.size() > 0) {
            res = attrGroups.stream().map(item -> {
                AttrGroupWithAttrsVo resp = new AttrGroupWithAttrsVo();
                BeanUtils.copyProperties(item, resp);
                List<AttrAttrgroupRelationEntity> relationEntities = relationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", resp.getAttrGroupId()));
                List<AttrEntity> attrs = null;
                if (relationEntities != null && relationEntities.size() > 0) {
                    List<Long> attrIds = relationEntities.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
                    attrs = attrService.listByIds(attrIds);
                }
                resp.setAttrs(attrs);
                return resp;
            }).collect(Collectors.toList());
        }
        return R.ok().put("data", res);
    }

    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrAttrgroupRelationEntity> entities) {
        relationService.saveBatch(entities);
        return R.ok();
    }


    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody AttrGroupRelationVo[] vos) {
        attrGroupService.deleteRelation(vos);
        return R.ok();
    }


    /**
     * 查询分组关联的属性
     *
     * @param attrgroupId
     * @return
     */
    @GetMapping("{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> entities = attrService.getRelation(attrgroupId);
        return R.ok().put("data", entities);
    }

    /**
     * /product/attrgroup/{attrgroupId}/noattr/relation
     * 查询分组没有关联的属性
     *
     * @param attrgroupId
     * @return
     */
    @GetMapping("{attrgroupId}/noattr/relation")
    public R attrNoRelation(@RequestParam Map<String, Object> params,
                            @PathVariable("attrgroupId") Long attrgroupId) {
        PageUtils page = attrService.getNoRelation(params, attrgroupId);
        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    //@RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params,
                  @PathVariable("catelogId") Long catelogId) {
        //PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params, catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    //@RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long catelogId = attrGroup.getCatelogId();
        Long[] path = categoryService.findCateLogPath(catelogId);
        attrGroup.setCatelogPath(path);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
