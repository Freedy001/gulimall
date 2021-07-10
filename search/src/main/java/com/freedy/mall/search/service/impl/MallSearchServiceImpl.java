package com.freedy.mall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.freedy.common.to.es.SkuEsModel;
import com.freedy.common.utils.R;
import com.freedy.mall.search.config.ElasticSearchConfig;
import com.freedy.mall.search.constant.EsConstant;
import com.freedy.mall.search.feign.ProductFeignService;
import com.freedy.mall.search.service.MallSearchService;
import com.freedy.mall.search.vo.AttrRespVo;
import com.freedy.mall.search.vo.BrandVo;
import com.freedy.mall.search.vo.SearchParam;
import com.freedy.mall.search.vo.SearchResult;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Freedy
 * @date 2021/3/2 19:17
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public SearchResult search(SearchParam param) {
        SearchResult searchResult = new SearchResult();
        //准备检索请求
        SearchRequest searchRequest = buildSearchRequest(param);
        try {
            //执行检索请求
            SearchResponse response = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            //分析响应数据封装成我们需要的格式
            searchResult = buildSearchResult(response, param);
            System.out.println(JSON.toJSONString(searchResult));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return searchResult;
    }


    /**
     * 准备检索请求,动态构建出查询所需要的DSL语句
     * 模糊匹配、过滤（按照属性，分类，品牌、价格区间、库存），排序，分页，高亮，聚合分析
     *
     * @return
     */
    private SearchRequest buildSearchRequest(SearchParam param) {
        //构建DSL
        SearchSourceBuilder builder = new SearchSourceBuilder();
        /**
         * 查询：模糊匹配、过滤（按照属性，分类，品牌、价格区间、库存）
         */
        //模糊匹配
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }
        //条件查询
        boolQuery.filter(QueryBuilders.termQuery("hasStock", param.getHasStock() != 0));
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }
        //价格区间
        if (!StringUtils.isEmpty(param.getSkuPrice())) {
            RangeQueryBuilder skuPrice = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (param.getSkuPrice().startsWith("_")) {
                skuPrice.lte(s[1]);
            } else if (param.getSkuPrice().endsWith("_")) {
                skuPrice.gte(s[0]);
            } else {
                skuPrice.gte(s[0]).lte(s[1]);
            }
            boolQuery.filter(skuPrice);
        }
        //按照指定的属性查询
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            for (String attr : param.getAttrs()) {
                BoolQueryBuilder nestedBool = QueryBuilders.boolQuery();
                String[] s = attr.split("_");
                String attrId = s[0];
                String[] attrValue = s[1].split(":");
                nestedBool.must(QueryBuilders.termQuery("attrs.attrId", attrId));
                nestedBool.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));
                NestedQueryBuilder attrs = QueryBuilders.nestedQuery("attrs", nestedBool, ScoreMode.None);
                boolQuery.filter(attrs);
            }
        }
        builder.query(boolQuery);
        /**
         * 排序，分页，高亮
         */
        if (!StringUtils.isEmpty(param.getSort())) {
            String[] s = param.getSort().split("_");
            builder.sort(s[0], SortOrder.fromString(s[1]));
        }
        builder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        builder.size(EsConstant.PRODUCT_PAGESIZE);
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuTitle");
            highlightBuilder.preTags("<b style='color:red'>");
            highlightBuilder.postTags("</b>");
            builder.highlighter(highlightBuilder);
        }
        /**
         * 聚合分析
         */
        /*
        品牌聚合
         */
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg");
        brand_agg.field("brandId");
        brand_agg.size(50);
        //聚合出对应品牌的名字
        TermsAggregationBuilder brand_name_agg = AggregationBuilders.terms("brand_name_agg");
        brand_name_agg.field("brandName");
        brand_name_agg.size(1);
        //聚合出对应品牌的logo
        TermsAggregationBuilder brand_img_agg = AggregationBuilders.terms("brand_img_agg");
        brand_img_agg.field("brandImg");
        brand_img_agg.size(1);
        brand_agg.subAggregation(brand_name_agg);
        brand_agg.subAggregation(brand_img_agg);
        builder.aggregation(brand_agg);
        /*
        分类聚合
         */
        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg");
        catalog_agg.field("catalogId");
        catalog_agg.size(20);
        //聚合出对应分类id的名字
        TermsAggregationBuilder catalog_name_agg = AggregationBuilders.terms("catalog_name_agg");
        catalog_name_agg.field("catalogName");
        catalog_name_agg.size(1);
        catalog_agg.subAggregation(catalog_name_agg);
        builder.aggregation(catalog_agg);
        /*
        属性聚合
         */
        NestedAggregationBuilder nested = AggregationBuilders.nested("attr_agg", "attrs");
        //聚合出当前所有的attrId
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg");
        attr_id_agg.field("attrs.attrId");
        attr_id_agg.size(10);
        //聚合出对应属性名字
        TermsAggregationBuilder attr_name_agg = AggregationBuilders.terms("attr_name_agg");
        attr_name_agg.field("attrs.attrName");
        attr_name_agg.size(1);
        //聚合出对应属性的值
        TermsAggregationBuilder attr_value_agg = AggregationBuilders.terms("attr_value_agg");
        attr_value_agg.field("attrs.attrValue");
        attr_value_agg.size(10);
        attr_id_agg.subAggregation(attr_name_agg);
        attr_id_agg.subAggregation(attr_value_agg);
        nested.subAggregation(attr_id_agg);
        builder.aggregation(nested);

        System.out.println("=================构建的DSL=================");
        System.out.println(builder);
        return new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, builder);
    }

    /**
     * 构建结果数据
     *
     * @param response
     * @return
     */
    private SearchResult buildSearchResult(SearchResponse response, SearchParam param) {
        SearchResult result = new SearchResult();
        SearchHits hits = response.getHits();
        List<SkuEsModel> skuEsModelList = new ArrayList<>();
        for (SearchHit hit : hits.getHits()) {
            String sourceAsString = hit.getSourceAsString();
            SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
            if (hit.getHighlightFields().get("skuTitle") != null) {
                skuEsModel.setSkuTitle(hit.getHighlightFields().get("skuTitle").getFragments()[0].string());
            }
            skuEsModelList.add(skuEsModel);
        }
        //返回所有查询到的商品
        result.setProduct(skuEsModelList);
        //当前商品所涉及到的分类信息
        ParsedLongTerms catalog_agg = response.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogs = new ArrayList<>();
        for (Terms.Bucket catalogBucket : catalog_agg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            catalogVo.setCatalogId(Long.valueOf(catalogBucket.getKeyAsString()));
            //获取分类对应的名字
            ParsedStringTerms catalog_name_agg = catalogBucket.getAggregations().get("catalog_name_agg");
            catalogVo.setCatalogName(catalog_name_agg.getBuckets().get(0).getKeyAsString());
            catalogs.add(catalogVo);
        }
        result.setCatalogs(catalogs);
        //当前商品所涉及到的品牌信息
        ParsedLongTerms brand_agg = response.getAggregations().get("brand_agg");
        List<SearchResult.BrandVo> brands = new ArrayList<>();
        for (Terms.Bucket brandBucket : brand_agg.getBuckets()) {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            brandVo.setBrandId(Long.valueOf(brandBucket.getKeyAsString()));
            //设置品牌id对应的名称
            ParsedStringTerms brand_name_agg = brandBucket.getAggregations().get("brand_name_agg");
            brandVo.setBrandName(brand_name_agg.getBuckets().get(0).getKeyAsString());
            //设置品牌id对应的logo
            ParsedStringTerms brand_img_agg = brandBucket.getAggregations().get("brand_img_agg");
            brandVo.setBrandImg(brand_img_agg.getBuckets().get(0).getKeyAsString());
            brands.add(brandVo);
        }
        result.setBrands(brands);
        ParsedNested attr_agg = response.getAggregations().get("attr_agg");
        ParsedLongTerms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        ArrayList<SearchResult.AttrVo> attrVos = new ArrayList<>();
        for (Terms.Bucket attrBucket : attr_id_agg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            attrVo.setAttrId(Long.valueOf(attrBucket.getKeyAsString()));
            //获取属性id对应的名称
            ParsedStringTerms attr_name_agg = attrBucket.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attr_name_agg.getBuckets().get(0).getKeyAsString());
            //获取属性id对应值
            ParsedStringTerms attr_value_agg = attrBucket.getAggregations().get("attr_value_agg");
            ArrayList<String> AttrValues = new ArrayList<>();
            for (Terms.Bucket attrValueBucket : attr_value_agg.getBuckets()) {
                AttrValues.add(attrValueBucket.getKeyAsString());
            }
            attrVo.setAttrValue(AttrValues);
            attrVos.add(attrVo);
        }
        //当前商品所涉及到的属性信息
        result.setAttrs(attrVos);
        //分页信息 当前页码
        result.setPageNum(param.getPageNum());
        //分页信息 总记录数
        result.setTotal(hits.getTotalHits().value);
        //分页信息 总页码
        result.setTotalPage((int) Math.ceil((double) hits.getTotalHits().value / (double) EsConstant.PRODUCT_PAGESIZE));
        //构建面包屑导航功能
        if (param.getAttrs() != null && param.getAttrs().size() > 0) {
            List<SearchResult.NavVo> collect = param.getAttrs().stream().map(attr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String val = attr.split("_")[0];
                R info = productFeignService.attrInfo(Long.parseLong(val));
                if (info.getCode() == 0) {
                    AttrRespVo AttrResp = info.getData("attr", new TypeReference<AttrRespVo>() {
                    });
                    navVo.setNavName(AttrResp.getAttrName());
                } else {
                    navVo.setNavName(val);
                }
                navVo.setNavValue(attr.split("_")[1]);
                try {
                    String replace = param.getQueryString().replace("&attrs=" + URLEncoder.encode(attr, "utf-8").replace("+", "%20"), "");
                    navVo.setLink("http://search.freedymall.com/list.html?" + replace);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return navVo;
            }).collect(Collectors.toList());
            result.setNavs(collect);
        }
        if (param.getBrandId() != null && param.getBrandId().size() > 0) {
            List<SearchResult.NavVo> navs = result.getNavs();
            R r = productFeignService.brandInfo(param.getBrandId());
            if(r.getCode()==0){
                List<BrandVo> data = r.getData(new TypeReference<List<BrandVo>>() {
                });
                for (BrandVo datum : data) {
                    SearchResult.NavVo navVo = new SearchResult.NavVo();
                    navVo.setNavName("品牌");
                    navVo.setNavValue(datum.getName());
                    try {
                        String replace = param.getQueryString().replace("&brandId=" + URLEncoder.encode(String.valueOf(datum.getBrandId()), "utf-8").replace("+", "%20"), "");
                        navVo.setLink("http://search.freedymall.com/list.html?" + replace);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    navs.add(navVo);
                }
            }
        }
        return result;
    }

}

