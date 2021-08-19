package com.delicacy.miniapp.service.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.delicacy.miniapp.service.service.AbstractService;
import com.delicacy.miniapp.service.service.AnalysisStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * @author yutao.zhang
 * @create 2021-07-28 15:21
 **/
@Service
public class AnalysisStockServiceImpl extends AbstractService implements AnalysisStockService {

    @Autowired
    protected MongoTemplate mongoTemplate;


    @Override
    public List<Map> listByFilter(String... symbols) {
        stock(true);
        Query query = new Query();
        if (!isEmpty(symbols)) {
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("symbol").in(symbols)
            ));
        }

        List<Map> maps = mongoTemplate.find(query, Map.class, "analysis_astock");
        return maps;
    }

    @Override
    public List<Map> list(String... symbols) {
        stock(false);
        Query query = new Query();
        if (!isEmpty(symbols)) {
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("symbol").in(symbols)
            ));
        }
        List<Map> maps = mongoTemplate.find(query, Map.class, "analysis_astock");
        return maps;
    }

    public void stock(Boolean isfilter) {
        String analysis_table = "analysis_astock";
        dropCollection(analysis_table);

        Query query = new Query();
        if (isfilter){
            query.addCriteria(new Criteria().andOperator(
                    Criteria.where("$where").is("this.shiyinglv_TTM * 1 < this.shiyinglv_jing * 1"),
                    Criteria.where("$where").is("this.shiyinglv_TTM > 0"),
//                    Criteria.where("$where").is("this.current < 200"),
                    Criteria.where("$where").is("this.shiyinglv_dong * 1 < this.shiyinglv_jing * 1"),
                    Criteria.where("$where").is("this.shiyinglv_TTM < 120")
            ));
        }

        List<Map> maps = mongoTemplate.find(query, Map.class, "xueqiu_astock");

        maps.stream().filter(e -> {
            try {
                // Double guxilv_TTM = percentData(e.get("guxilv_TTM").toString());
                Double zongshizhi = moneyData(e.get("zongshizhi").toString());
                return zongshizhi > 2000000000L; //&& guxilv_TTM > 0.5;//50亿
            } catch (IllegalArgumentException e1) {
                return false;
            }
        }).forEach(e -> {
            Query q = new Query();
            q.addCriteria(new Criteria().andOperator(
                    Criteria.where("symbol").is(e.get("symbol"))
            ));
            List<Map> xueqiu_astock_desc = mongoTemplate.find(q, Map.class, "xueqiu_astock_desc");
            if (!isEmpty(xueqiu_astock_desc)){
                e.put("platename",xueqiu_astock_desc.get(0).get("platename"));
            }
            if (!isEmpty(e.get("52zhouzuidi"))&&!isEmpty(e.get("52zhouzuigao"))&&!isEmpty(e.get("current"))){
                String aa = String.valueOf(e.get("52zhouzuidi"));
                String bb = String.valueOf(e.get("52zhouzuigao"));
                String cc = String.valueOf(e.get("current"));
                String s = BigDecimal.valueOf(Double.parseDouble(cc) - Double.parseDouble(aa)).divide(
                             BigDecimal.valueOf(Double.parseDouble(bb) - Double.parseDouble(aa)),3, RoundingMode.UP
                    ).setScale(3, RoundingMode.UP).toString();
                e.put("gaodi",s);
            }


            e.put("symbol", String.valueOf(e.get("symbol")).replace("SZ", "").replace("SH", ""));
            addData(e, analysis_table);
        });
    }
}
