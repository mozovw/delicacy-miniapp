package com.delicacy.miniapp.service.processor.aijijin;

import cn.hutool.http.HttpUtil;
import com.delicacy.miniapp.service.processor.AbstactProcessor;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.JsonPathSelector;
import us.codecraft.webmagic.selector.Selectable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AiFundPositionProcessor extends AbstactProcessor {


    public static final String URL_REGEX = "http://fund.10jqka.com.cn";
    public static final String URL_POST = "http://fund.10jqka.com.cn/%s/portfolioindex.html";


    @Override
    public void process(Page page) {

        if (page.getUrl().regex(URL_REGEX).match()) {


            List<Selectable> nodes = page.getHtml().$("#zcgList .s-list ul").nodes();
            LinkedHashMap<Integer, LinkedHashMap<String, String>> mapMain = Maps.newLinkedHashMap();

            for (int i = 1; i < nodes.size(); i++) {
                List<Selectable> li = nodes.get(i).$("li").nodes();
                LinkedHashMap<String, String> map = Maps.newLinkedHashMap();

                String[] hrefs = page.getHtml().$(".t-line .list a", "href").get().split("/");
                if (hrefs.length == 5) {
                    map.put("fund_code", hrefs[3]);
                }

                map.put("fund_name", page.getHtml().$("title", "text").get());

                String[] split = page.getHtml().$(".dbox .date", "text").get().split(" ");
                if (split.length == 2) {
                    map.put("data_update_time", split[1]);
                }

                map.put("xuhao", li.get(0).$("li", "text").get());

                List<String> strings = HttpUtil.decodeParams(li.get(1).$("a", "href").get(), "utf-8").get("param");
                if (!isEmpty(strings)) {
                    map.put("gupiaodaima", strings.get(0).replace("'", ""));
                }

                map.put("gupiaomingcheng", li.get(1).$("li a", "text").get());
                map.put("chiyouliang(wangu)", li.get(2).$("li", "text").get());
                map.put("shizhi(wanyuan)", li.get(3).$("li", "text").get());
                map.put("zhanjingzhibi", li.get(4).$("li", "text").get());

                mapMain.put(i, map);
            }

            page.putField("map", mapMain);


        } else if (!ObjectUtils.isEmpty(page.getRawText())) {
            String rawText = page.getRawText();
            rawText = rawText.substring(2, rawText.length() - 1);
            List<String> strings = new JsonPathSelector("$.data.data").selectList(rawText);
            String string = strings.get(0);
            Map json2Obj = json2Obj(string, Map.class);
            ArrayList<Map<String, String>> list = new ArrayList<>(json2Obj.values());
            LinkedHashMap<Integer, LinkedHashMap<String, String>> mapMain = Maps.newLinkedHashMap();
            for (int i = 0; i < list.size(); i++) {
                Map<String, String> em = list.get(i);
                String shstat = em.get("shstat");
                if (!"开放".equals(shstat)) {
                    continue;
                }
                page.addTargetRequest(String.format(URL_POST, em.get("code")));

            }
        }


    }


}
