package com.delicacy.miniapp.service.pipeline;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;
import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;
import us.codecraft.webmagic.utils.FilePersistentBase;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class Map3MongoPipeline extends FilePersistentBase implements Pipeline {

    Snowflake snowflake = IdUtil.createSnowflake(1, 1);
    private MongoTemplate mongoTemplate;
    private String collectName;
    private String[] arrTxt;


    public Map3MongoPipeline(MongoTemplate mongoTemplate, String collectName) {
        this.mongoTemplate = mongoTemplate;
        this.collectName = collectName;
    }

    public Map3MongoPipeline(MongoTemplate mongoTemplate, String collectName, String... arrTxt) {
        this.mongoTemplate = mongoTemplate;
        this.collectName = collectName;
        this.arrTxt = arrTxt;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        Map<String, Object> all = resultItems.getAll();
        if (!all.isEmpty()) {
            Object obj = all.get("map");
            LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<String, Object>>> maps = (LinkedHashMap<Integer, LinkedHashMap<Integer, LinkedHashMap<String, Object>>>) obj;
            maps.entrySet().stream().forEach(e -> {
                e.getValue().entrySet().stream().forEach(ee -> {
                    save(ee.getValue());
                });
            });
        }
    }

    private void save(LinkedHashMap<String, Object> all) {
        if (ObjectUtils.isEmpty(all)) {
            return;
        }
        String string = JSON.toJSONString(all);
        String sign = DigestUtils.md5DigestAsHex(string.getBytes());
        Query query = new Query(Criteria.where("sign").is(sign));
        boolean exists = mongoTemplate.exists(query, Map.class, this.collectName);
        if (exists) {
            log.info("已经存在");
            return;
        }

        all.put("sign", sign);
        long id = snowflake.nextId();
        all.put("id", id);
        // update
        if (!ObjectUtils.isEmpty(arrTxt)) {
            Criteria criteria = Criteria.where("1").is(all.get("1"));
            Arrays.stream(arrTxt).forEach(e -> {
                criteria.and(e).is(all.get(e).toString());
            });
            exists = mongoTemplate.exists(new Query(criteria), Map.class, this.collectName);
            if (exists) {
                Update update = new Update();
                all.entrySet().stream().forEach(e -> {
                    update.set(e.getKey(), e.getValue());
                });
                mongoTemplate.updateFirst(query, update, collectName);
                return;
            }
        }
        mongoTemplate.save(all, collectName);
    }
}
