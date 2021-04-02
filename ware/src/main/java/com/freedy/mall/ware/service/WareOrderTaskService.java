package com.freedy.mall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.freedy.common.utils.PageUtils;
import com.freedy.mall.ware.entity.WareOrderTaskEntity;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * 库存工作单
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:45:34
 */
public interface WareOrderTaskService extends IService<WareOrderTaskEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

