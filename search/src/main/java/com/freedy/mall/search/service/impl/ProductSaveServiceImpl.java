package com.freedy.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.freedy.common.to.es.SkuEsModel;
import com.freedy.mall.search.config.ElasticSearchConfig;

import com.freedy.mall.search.constant.EsConstant;
import com.freedy.mall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Freedy
 * @date 2021/2/10 15:58
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Qualifier("esRestClient")
    @Autowired
    RestHighLevelClient client;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel skuEsModel : skuEsModels) {
            IndexRequest indexRequest = new IndexRequest(EsConstant.PRODUCT_INDEX);
            indexRequest.id(skuEsModel.getSkuId().toString());
            String jsonString = JSON.toJSONString(skuEsModel);
            indexRequest.source(jsonString, XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulk = client.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);
        if (bulk.hasFailures()){
            List<String> error = Arrays.stream(bulk.getItems()).map(BulkItemResponse::getFailureMessage).collect(Collectors.toList());
            log.error("商品上架错误:{}",error);
            return false;
        }
        return true;
    }
}
