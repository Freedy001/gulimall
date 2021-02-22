package com.freedy.mall.product;

import com.freedy.mall.product.entity.AttrAttrgroupRelationEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductApplicationTests {

    @Test
    public void test(){
        AttrAttrgroupRelationEntity a1 = new AttrAttrgroupRelationEntity();
        AttrAttrgroupRelationEntity a2 = new AttrAttrgroupRelationEntity();
        a1.setAttrId(1L);
        a1.setAttrGroupId(1L);
        a1.setId(1L);
        BeanUtils.copyProperties(a1,a2);
        System.out.println(a1);
        System.out.println(a2);
    }
}
