package com.freedy.mall.search.service;

import com.freedy.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * @author Freedy
 * @date 2021/2/10 15:56
 */
public interface ProductSaveService {
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
