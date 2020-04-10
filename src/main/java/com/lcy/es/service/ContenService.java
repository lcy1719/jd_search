package com.lcy.es.service;

import com.alibaba.fastjson.JSON;
import com.lcy.es.POJO.Goods;
import com.lcy.es.utils.HtmlParseUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@Service
public class ContenService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private HtmlParseUtils htmlParseUtils;

    //入库
    public boolean addall(String keyword) throws IOException {
        ArrayList<Goods> list = htmlParseUtils.getlist(keyword);
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < list.size(); i++) {
            bulkRequest.add(new IndexRequest("jd_index").source(JSON.toJSONString(list.get(i)), XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    public ArrayList<Map<String, Object>> getall(String keyword, Integer page, Integer num) throws IOException {
        SearchRequest request = new SearchRequest("jd_index");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("title", keyword);
        sourceBuilder.query(termQueryBuilder);
        request.source(sourceBuilder);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        ArrayList<Map<String,Object>> list=new ArrayList<>();
        for (SearchHit fields : response.getHits().getHits()) {
            Map<String, Object> sourceAsMap = fields.getSourceAsMap();
            list.add(sourceAsMap);
        }
        return list;
    }
}
