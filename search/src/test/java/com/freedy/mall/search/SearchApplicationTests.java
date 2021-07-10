
package com.freedy.mall.search;

import com.alibaba.fastjson.JSON;
import com.freedy.mall.search.config.ElasticSearchConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//@Slf4j
//@RunWith(SpringRunner.class)
//@SpringBootTest
public class SearchApplicationTests {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private RestHighLevelClient client;

    @Test
    public void searchData() throws IOException {
        //创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        //指定索引
        searchRequest.indices("bank");
        //指定DSL，检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //构造检索条件
        searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
        searchSourceBuilder.aggregation(AggregationBuilders.terms("ageAgg").field("age").size(10));
        searchSourceBuilder.aggregation(AggregationBuilders.avg("balanceAvg").field("balance"));
       // searchSourceBuilder.aggregation();
        searchRequest.source(searchSourceBuilder);
        //执行检索
        SearchResponse searchResponse = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
        //分析结果
        System.out.println(searchResponse);
        //Map map = JSON.parseObject(searchResponse.toString(), Map.class);
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            Account account = JSON.parseObject(hit.getSourceAsString(), Account.class);
            System.out.println(JSON.toJSONString(account,true));
        }
        Terms ageAgg = searchResponse.getAggregations().get("ageAgg");
        for (Terms.Bucket bucket : ageAgg.getBuckets()) {
            System.out.println(bucket.getKeyAsString()+"-----"+bucket.getDocCount());
        }
        Avg balance = searchResponse.getAggregations().get("balanceAvg");
        System.out.println(balance.getValue());
    }

    @Test
    public void contextLoads() throws IOException {
        IndexRequest indexRequest = new IndexRequest("user");
        indexRequest.id("1");

        User user = new User();
        user.setUsername("小明");
        user.setAge(12);
        user.setGender("男");

        String json = JSON.toJSONString(user);
        indexRequest.source(json, XContentType.JSON);
        IndexResponse index = client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Test
    public void test(){
        String a="500_";
        System.out.println(a.split("_").length);
        System.out.println(Arrays.toString(a.split("_")));
    }

    @Data
     class User{
        private String username;
        private String gender;
        private Integer age;
    }
}
