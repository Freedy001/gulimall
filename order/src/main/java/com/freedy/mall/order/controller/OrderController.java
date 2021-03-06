package com.freedy.mall.order.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.freedy.mall.order.entity.OrderEntity;
import com.freedy.mall.order.service.OrderService;
import com.freedy.common.utils.PageUtils;
import com.freedy.common.utils.R;



/**
 * 订单
 *
 * @author freedy
 * @email 985948228@qq.com
 * @date 2021-01-30 21:39:24
 */
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @PostMapping("/listWithItem")
    public R listWithItem(@RequestBody Map<String, Object> params){
        PageUtils page = orderService.queryPageWithItem(params);
        return R.ok().setData(page);
    }

    @GetMapping("/status/{orderSn}")
    public R getOrderStatus(@PathVariable String orderSn){
        OrderEntity entity=orderService.getOrderByOrderSn(orderSn);
        return R.ok().setData(entity);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("order:order:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("order:order:info")
    public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("order:order:save")
    public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("order:order:update")
    public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("order:order:delete")
    public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
