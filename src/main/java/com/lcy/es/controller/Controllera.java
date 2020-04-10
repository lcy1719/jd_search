package com.lcy.es.controller;

import com.lcy.es.service.ContenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@Controller
public class Controllera {
    @Autowired
    private ContenService contenService;

    @GetMapping("/index")
    public String index() {
        return "index";
    }

    //入库
    @GetMapping("/parse/{keyword}")
    @ResponseBody
    public boolean addall(@PathVariable("keyword") String keyword) throws IOException {
        boolean addall = contenService.addall(keyword);
        return addall;
    }

    //查找
    @GetMapping("/get/{keyword}/{page}/{num}")
    @ResponseBody
    public ArrayList<Map<String, Object>> get(@PathVariable("keyword") String keyword,
                                              @PathVariable("page") Integer page,
                                              @PathVariable("num") Integer num) throws IOException {
       return contenService.getall(keyword,page,num);
    }
}
