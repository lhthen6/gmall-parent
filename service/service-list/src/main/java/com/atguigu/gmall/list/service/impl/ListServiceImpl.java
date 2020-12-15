package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.repository.GoodsElasticsearchRepository;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private GoodsElasticsearchRepository goodsElasticsearchRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<JSONObject> getBaseCategoryList() {
        return productFeignClient.getBaseCategoryList();
    }

    @Override
    public void onSale(Long skuId) {
        Goods goods = new Goods();
        // sku数据
        SkuInfo skuInfo = productFeignClient.getSkuInfoBySkuId(skuId);
        // 属性数据
        List<SearchAttr> searchAttrs = productFeignClient.getSearchAttrList(skuId);
        // 商标数据
        BaseTrademark baseTrademark = productFeignClient.getTrademarkById(skuInfo.getTmId());

        goods.setTitle(skuInfo.getSkuName());
        goods.setHotScore(0l);
        goods.setCategory3Id(skuInfo.getCategory3Id());
        goods.setCreateTime(new Date());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setTmId(baseTrademark.getId());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        goods.setTmName(baseTrademark.getTmName());
        goods.setAttrs(searchAttrs);

        goods.setId(skuId);
        goodsElasticsearchRepository.save(goods);
    }

    @Override
    public void cancelSale(Long skuId) {
        goodsElasticsearchRepository.deleteById(skuId);
    }

    @Override
    public void createGoodsIndex() {
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
    }

    @Override
    public SearchResponseVo list(SearchParam searchParam) {
        // 需要解析的返回结果
        SearchResponse searchResponse = null;
        // 需要封装的请求语句
        SearchRequest searchRequest = getSearchRequest(searchParam);
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 解析返回结果
        SearchResponseVo searchResponseVo = parseSearchResponse(searchResponse);
        return searchResponseVo;
    }

    @Override
    public void hotScore(Long skuId) {
        // 将热度值更新进缓冲区
        Integer hotScoreRedis = (Integer) redisTemplate.opsForValue().get("hotScore:" + skuId);
        if (null != hotScoreRedis) {
            hotScoreRedis++;
            redisTemplate.opsForValue().increment("hotScore:" + skuId, 1);
            if (hotScoreRedis % 10 == 0) {
                // 将热度值更新进es
                Goods goods = goodsElasticsearchRepository.findById(skuId).get();
                goods.setHotScore(Long.parseLong(hotScoreRedis + ""));
                goodsElasticsearchRepository.save(goods);
            }
        } else {
            redisTemplate.opsForValue().set("hotScore:" + skuId, 1);
        }

    }

    private SearchRequest getSearchRequest(SearchParam searchParam) {
        SearchRequest searchRequest = new SearchRequest(); // 请求
        searchRequest.indices("goods");
        searchRequest.types("info");

        // 查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder(); // dsl

        // 参数
        Long category3Id = searchParam.getCategory3Id();
        String keyword = searchParam.getKeyword();
        String[] props = searchParam.getProps();// 属性id:属性值名称:属性名称
        String trademark = searchParam.getTrademark();// 商标id:商标名称
        String order = searchParam.getOrder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        // 关键字
        if (!StringUtils.isEmpty(keyword)) {
            MatchQueryBuilder title = new MatchQueryBuilder("title", keyword);
            boolQueryBuilder.must(title);
        }

        // 商标
        if (!StringUtils.isEmpty(trademark)) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("tmId", trademark.split(":")[0]);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        // 属性
        if (null != props && props.length > 0) {
            for (String prop : props) {
                String[] split = prop.split(":");
                Long attrId = Long.parseLong(split[0]);
                String attrValue = split[1];
                String attrName = split[2];

                BoolQueryBuilder boolQueryBuilderNested = new BoolQueryBuilder();
                TermQueryBuilder termQueryBuilderAttrId = new TermQueryBuilder("attrs.attrId", attrId);
                boolQueryBuilderNested.filter(termQueryBuilderAttrId);
                MatchQueryBuilder matchQueryBuilderAttrValue = new MatchQueryBuilder("attrs.attrValue", attrValue);
                boolQueryBuilderNested.must(matchQueryBuilderAttrValue);
                MatchQueryBuilder matchQueryBuilderAttrName = new MatchQueryBuilder("attrs.attrName", attrName);
                boolQueryBuilderNested.must(matchQueryBuilderAttrName);

                NestedQueryBuilder nestedQueryBuilder = new NestedQueryBuilder("attrs", boolQueryBuilderNested, ScoreMode.None);

                boolQueryBuilder.filter(nestedQueryBuilder);
            }
        }

        // 三级分类
        if (null != category3Id && category3Id > 0) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("category3Id", category3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }
        searchSourceBuilder.query(boolQueryBuilder);

        // 商标聚合
        TermsAggregationBuilder termsTmAggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        searchSourceBuilder.aggregation(termsTmAggregationBuilder);

        // 属性聚合
        NestedAggregationBuilder nestedAttrAggregationBuilder = AggregationBuilders.nested("attrsAgg", "attrs").subAggregation(
                AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue")));
        searchSourceBuilder.aggregation(nestedAttrAggregationBuilder);

        // 页面size
        searchSourceBuilder.size(20);
        searchSourceBuilder.from(0);

        // 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style='color:red;font-weight:bolder'>");
        highlightBuilder.field("title");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        // 排序
        if (!StringUtils.isEmpty(order)) {
            String key = order.split(":")[0];
            String sort = order.split(":")[1];

            String sortName = "hotScore";

            if (key.equals("2")) {
                sortName = "price";
            }

            searchSourceBuilder.sort(sortName, sort.equals("asc") ? SortOrder.ASC : SortOrder.DESC);
        }

        System.out.println(searchSourceBuilder.toString());
        searchRequest.source(searchSourceBuilder);

        return searchRequest;
    }

    private SearchResponseVo parseSearchResponse(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchHits hits = searchResponse.getHits(); // 概览
        SearchHit[] hitsResult = hits.getHits(); //结果
        if (null != hitsResult && hitsResult.length > 0) {
            List<Goods> list = new ArrayList<>();
            for (SearchHit document : hitsResult) {
                // 获取每条数据的json
                String json = document.getSourceAsString();
                Goods goods = JSONObject.parseObject(json, Goods.class);
                // 获取高亮
                Map<String, HighlightField> highlightFields = document.getHighlightFields();
                if (null != highlightFields) {
                    HighlightField highlightTitle = highlightFields.get("title");
                    if (null != highlightTitle) {
                        String title = highlightTitle.getFragments()[0].toString();
                        goods.setTitle(title);
                    }
                }
                list.add(goods);
            }
            searchResponseVo.setGoodsList(list);// 商品集合

            //List<SearchResponseTmVo> searchResponseTmVos = new ArrayList<>();
            ParsedLongTerms tmIdAgg = searchResponse.getAggregations().get("tmIdAgg");
            // 普通的循环遍历
            /*List<? extends Terms.Bucket> buckets = tmIdAgg.getBuckets();
            for (Terms.Bucket bucket : buckets) {
                SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
                // id
                long key = bucket.getKeyAsNumber().longValue();
                searchResponseTmVo.setTmId(key);
                // name
                ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
                List<? extends Terms.Bucket> tmNameAggBuckets = tmNameAgg.getBuckets();
                String tmNameKey = tmNameAggBuckets.get(0).getKeyAsString();
                searchResponseTmVo.setTmName(tmNameKey);
                //url
                ParsedStringTerms tmLogoUrlAgg = bucket.getAggregations().get("tmLogoUrlAgg");
                List<? extends Terms.Bucket> tmLogoUrlAggBuckets = tmLogoUrlAgg.getBuckets();
                String tmLogoUrlKey = tmLogoUrlAggBuckets.get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(tmLogoUrlKey);

                searchResponseTmVos.add(searchResponseTmVo);
            }*/
            // 流式
            List<SearchResponseTmVo> searchResponseTmVos = tmIdAgg.getBuckets().stream().map(tmBucket -> {
                SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
                // id
                long key = tmBucket.getKeyAsNumber().longValue();
                searchResponseTmVo.setTmId(key);
                // name
                ParsedStringTerms tmNameAgg = tmBucket.getAggregations().get("tmNameAgg");
                String tmNameKey = tmNameAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmName(tmNameKey);
                // url
                ParsedStringTerms tmLogoUrlAgg = tmBucket.getAggregations().get("tmLogoUrlAgg");
                String tmLogoUrlKey = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
                searchResponseTmVo.setTmLogoUrl(tmLogoUrlKey);

                return searchResponseTmVo;
            }).collect(Collectors.toList());
            searchResponseVo.setTrademarkList(searchResponseTmVos);// 品牌集合

            // 解析属性聚合函数
            ParsedNested attrsAgg = searchResponse.getAggregations().get("attrsAgg");
            ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
            List<SearchResponseAttrVo> searchResponseAttrVos = attrIdAgg.getBuckets().stream().map(attrIdBucket -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                // id
                long attrIdKey = attrIdBucket.getKeyAsNumber().longValue();
                // name
                ParsedStringTerms attrNameAgg = attrIdBucket.getAggregations().get("attrNameAgg");
                String attrNameKey = attrNameAgg.getBuckets().get(0).getKeyAsString();
                // ValueList
                ParsedStringTerms attrValueAgg = attrIdBucket.getAggregations().get("attrValueAgg");
                List<String> attrValueList = attrValueAgg.getBuckets().stream().map(attrValueBucket -> {
                    String attrValueKey = attrValueBucket.getKeyAsString();
                    return attrValueKey;
                }).collect(Collectors.toList());

                searchResponseAttrVo.setAttrId(attrIdKey);
                searchResponseAttrVo.setAttrName(attrNameKey);
                searchResponseAttrVo.setAttrValueList(attrValueList);
                return searchResponseAttrVo;
            }).collect(Collectors.toList());

            searchResponseVo.setAttrsList(searchResponseAttrVos);// 属性集合
        }
        return searchResponseVo;
    }

}
