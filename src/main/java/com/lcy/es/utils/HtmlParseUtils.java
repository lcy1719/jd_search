package com.lcy.es.utils;

import com.lcy.es.POJO.Goods;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
@Component
public class HtmlParseUtils {

    public ArrayList<Goods> getlist(String keyword) throws IOException {
        //id----J_goodsList
        //ul-----gl-warp clearfix
        //url-----https://search.jd.com/Search?keyword=java
        //attr----attr()是获取或设置某个元素的属性。
        //eq(数字)-----获取当前标签的第（参数+1）个的内容,比如一个页面有多个a标签，getElementsByTag("a").eq(1)就是获取第2个a标签的内容
        //查询的网站url
        String url="https://search.jd.com/Search?keyword="+keyword;
        //解析网站
        Document document = Jsoup.parse(new URL(url), 30000);
        //获得承载信息的div
        Element element = document.getElementById("J_goodsList");
        //获得列表也就是li
        Elements lis = element.getElementsByTag("li");
        //创建一个列表
        ArrayList<Goods> list = new ArrayList<>();
        for (Element li : lis) {
            String img = li.getElementsByTag("img").eq(0).attr("source-data-lazy-img");
            String title = li.getElementsByClass("p-name").eq(0).text();
            String price = li.getElementsByClass("p-price").eq(0).text();
            String shop = li.getElementsByClass("J_im_icon").eq(0).text();
            Goods goods = new Goods();
            goods.setTitle(title);
            goods.setImg(img);
            goods.setPrice(price);
            goods.setShop(shop);
            list.add(goods);
        }
        return list;
    }
}
