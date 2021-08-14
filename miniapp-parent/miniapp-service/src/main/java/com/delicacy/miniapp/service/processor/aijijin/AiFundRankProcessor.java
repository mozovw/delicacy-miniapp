package com.delicacy.miniapp.service.processor.aijijin;

import com.delicacy.miniapp.service.processor.AbstactProcessor;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.selector.JsonPathSelector;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class AiFundRankProcessor extends AbstactProcessor {

    @Override
    public void process(Page page) {
        if (!ObjectUtils.isEmpty(page.getRawText())) {

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

                LinkedHashMap<String, String> map = Maps.newLinkedHashMap();

                map.put("symbol", em.get("code"));
                map.put("name", em.get("name"));
                map.put("type", em.get("type"));
                map.put("orgname", em.get("orgname"));
                map.put("date", em.get("newdate"));
                map.put("shengouzhuangtai", em.get("sgstat"));

                map.put("shuhuizhuangtai", shstat);
                map.put("danweijingzhi", em.get("newnet"));
                map.put("leijijingzhi", em.get("totalnet"));
                map.put("rizengzhanglv", em.get("rate"));
                map.put("jin1zhou", em.get("F003N_FUND33"));
                map.put("jin1yue", em.get("F008"));
                map.put("jin3yue", em.get("F009"));
                map.put("jin6yue", em.get("F010"));
                map.put("jin1nian", em.get("F011"));
                map.put("jin2nian", em.get("F014N_FUND33"));
                map.put("jin3nian", em.get("F015N_FUND33"));
                map.put("chenglilai", em.get("F012"));
                map.put("chengliriqi", em.get("clrq"));

                mapMain.put(i, map);
            }
            page.putField("map", mapMain);


        }
    }


}
