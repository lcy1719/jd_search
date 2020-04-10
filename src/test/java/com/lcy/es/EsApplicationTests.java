package com.lcy.es;
import	java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.lcy.es.POJO.Goods;
import com.lcy.es.POJO.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.index.seqno.RetentionLeaseActions;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class EsApplicationTests {
    @Autowired
    @Qualifier("restHighLevelClient")
    //用Autowired时，最好Qualifier指定方法名字
    private RestHighLevelClient client;

    //创建一个索引
    @Test
    void create() throws IOException {
        //1.创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("lcy_index");
        //2.客户端执行请求indicesClient，请求后获得响应。
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse);
    }
    //查找一个索引
    @Test
    void exist() throws IOException {
        //1.创建查询索引请求
        GetIndexRequest request = new GetIndexRequest("lcy_index");
        //2.执行请求
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
    //删除一个索引
    @Test
    void delete() throws IOException {
        //1.创建删除索引请求
        DeleteIndexRequest request = new DeleteIndexRequest("lcy_index");
        //2.执行请求
        AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
        //3.输出是否删除
        System.out.println(delete.isAcknowledged());
    }

    //添加一个文档
    @Test
    void createDoc() throws IOException {
        //1.创建一个索引请求
        IndexRequest request = new IndexRequest("lcy_index");
        //2.创建一个对象
        User user = new User("李晨阳", 22);
        //3.规则put /lcy_index/1
        request.id("1");
        request.timeout("1s");
        //4.把对象转换json字符串放到索引请求中
        request.source(JSON.toJSONString(user), XContentType.JSON);
        //5.执行请求，获得响应数据
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        //6.获得响应结果
        System.out.println(response.toString());
        System.out.println(response.status());
    }
    //判断一个文档是否存在
    @Test
    void existDoc() throws IOException {
        //1.创建一个文档请求,此举代码意思是lcy_index的第一个文档
        GetRequest getRequest = new GetRequest("lcy_index","1");
        //2.不获取文档的信息
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        //3.执行查询
        boolean exists = client.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }
    //获取文档的信息
    @Test
    void getDocument() throws IOException {
        //1.创建索引文档请求
        GetRequest request = new GetRequest("lcy_index", "1");
        //2.执行文档请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        //3.输出返回信息
        System.out.println(response);
        //4.输出数据
        System.out.println(response.getSourceAsMap());
    }
    //更新文档的信息
    @Test
    void updateDouble() throws IOException {
        //1.创建索引文档请求
        UpdateRequest request = new UpdateRequest("lcy_index", "1");
        //2.创建对象（要更改的数据）
        User user = new User("李晨阳改名字了", 23);
        request.timeout("1s");
        //3.将对象转换成json
        request.doc(JSON.toJSONString(user),XContentType.JSON);
        //4.执行操作
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }
    //删除一个文档的信息
    @Test
    void deleteDoc() throws IOException {
        DeleteRequest request = new DeleteRequest("lcy_index");
        request.id("1");
        request.timeout("1s");
        DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.status());
    }
    //批量添加文档
    @Test
    void addList() throws IOException {
        //批量查询命令
        BulkRequest request = new BulkRequest();
        //放到一个list里面
        ArrayList<User> list = new ArrayList<>();
        list.add(new User("lcy1", 1));
        list.add(new User("lcy2", 1));
        list.add(new User("lcy3", 1));
        list.add(new User("lcy4", 1));
        list.add(new User("lcy5", 1));
        //放入条件与命令
        for (int i = 0; i < list.size(); i++) {
            request.add(new IndexRequest("lcy_index")
                    .id(""+(i+1))
                    .source(JSON.toJSONString(list.get(i)),XContentType.JSON));
        }
        //执行命令
        BulkResponse bulk = client.bulk(request, RequestOptions.DEFAULT);
        System.out.println(!bulk.hasFailures());
    }

    //条件查询文档
    @Test
    void getDocumentBystation() throws IOException {
        //1.执行哪个库，用哪种命令
        SearchRequest request = new SearchRequest("lcy_index");
        //2.创建一个构造器
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //3.执行哪种命令，条件是什么
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("username", "lcy1");
        //4.构造器放入命令
        builder.query(termQueryBuilder);
        //5.执行构造器
        request.source(builder);
        //6.执行命令
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        //7.在hits里面查出数据
        for (SearchHit fields : response.getHits().getHits()) {
            System.out.println(fields.getSourceAsMap());
        }
    }
    @Test
    void ge2() throws IOException {
        //id----J_goodsList
        //ul-----gl-warp clearfix
        //url-----https://search.jd.com/Search?keyword=java
        //attr----attr()是获取或设置某个元素的属性。
        //eq(数字)-----获取当前标签的第（参数+1）个的内容,比如一个页面有多个a标签，getElementsByTag("a").eq(1)就是获取第2个a标签的内容
        //查询的网站url
        //String url="https://search.jd.com/Search?keyword="+keyword;
        String url="https://search.jd.com/Search?keyword=java";
        //解析网站
        Document document = Jsoup.parse(new URL(url), 30000);
        //获得承载信息的div
        Element element = document.getElementById("J_goodsList");
        //获得列表也就是li
        Elements lis = element.getElementsByTag("li");
        //创建一个列表
        ArrayList<Goods> list = new ArrayList<>();
        for (Element li : lis) {
            String shop = li.getElementsByClass("J_im_icon").eq(0).text();
            System.out.println(shop);
//            String img = li.getElementsByTag("img").eq(0).attr("source-data-lazy-img");
//            String title = li.getElementsByClass("p-name").eq(0).text();
//            String price = li.getElementsByClass("p-price").eq(0).text();
//            String shop = li.getElementsByClass("J_im_icon").eq(2).text();
//            Goods goods = new Goods();
//            goods.setTitle(title);
//            goods.setImg(img);
//            goods.setPrice(price);
//            goods.setShop(shop);
//            list.add(goods);
        }
    }
}
