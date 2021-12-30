package com.delicacy.miniapp.service.processor.jinrongcaifu;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.delicacy.miniapp.service.processor.AbstactProcessor;
import com.google.common.collect.Maps;
import org.springframework.util.ObjectUtils;
import us.codecraft.webmagic.Page;

import java.util.*;

/**
 * @author yutao
 * @create 2021-12-26 15:51
 **/
public class CaifuForcastReportProcessor  extends AbstactProcessor {
    @Override
    public void process(Page page) {
        if (!ObjectUtils.isEmpty(page.getRawText())) {

            String rawText = page.getRawText();

            Map json2Obj = json2Obj(rawText, Map.class);
            ArrayList<Map<String, String>> list =(ArrayList<Map<String, String>>)((Map) json2Obj.get("result")).get("data");
            LinkedHashMap<Integer, LinkedHashMap<String, String>> mapMain = Maps.newLinkedHashMap();
            for (int i = 0; i < list.size(); i++) {
                Map<String, String> em = list.get(i);

                LinkedHashMap<String, String> map = Maps.newLinkedHashMap();

                map.put("symbol", em.get("SECURITY_CODE"));
                map.put("name", em.get("SECURITY_NAME_ABBR"));
                String report_date = em.get("REPORT_DATE");
                DateTime dateTime = DateUtil.parseDateTime(report_date);

                map.put("report_date", getReportDate(dateTime));
                map.put("yugaoleixing", em.get("PREDICT_TYPE"));
                map.put("yucezhibiao", em.get("PREDICT_FINANCE"));
                map.put("yejibiandong", em.get("PREDICT_CONTENT"));
                map.put("yejibiandongyuanyin", em.get("CHANGE_REASON_EXPLAIN"));

                mapMain.put(i, map);
            }
            page.putField("map", mapMain);


        }
    }

    private String getReportDate(Date date){
        String mm = DateUtil.format(date, "MM");
        String yyyy = DateUtil.format(date, "yyyy");
        int m = Integer.parseInt(mm);
        switch (m){
            case 1:
            case 2:
            case 3: return yyyy+"一季报";
            case 4:
            case 5:
            case 6:return yyyy+"中报";
            case 7:
            case 8:
            case 9:return yyyy+"三季报";
            case 10:
            case 11:
            case 12:return yyyy+"年报";
        }
        return "";
    }
}
