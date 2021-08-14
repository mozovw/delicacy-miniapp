package com.delicacy.miniapp.service.pipeline;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

@Slf4j
public class MongoPipeline extends FilePersistentBase implements Pipeline {

    Snowflake snowflake = IdUtil.createSnowflake(1, 1);
    private MongoTemplate mongoTemplate;
    private String collectName;
    private ObjectMapper objectMapper = new ObjectMapper();
    private String[] arrTxt;

    public MongoPipeline(MongoTemplate mongoTemplate, String collectName) {
        this.mongoTemplate = mongoTemplate;
        this.collectName = collectName;
    }

    public MongoPipeline(MongoTemplate mongoTemplate, String collectName, String... arrTxt) {
        this.mongoTemplate = mongoTemplate;
        this.collectName = collectName;
        this.arrTxt = arrTxt;
    }
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void process(ResultItems resultItems, Task task) {
        Map<String, Object> all = resultItems.getAll();
        if (!all.isEmpty()) {
            try {
                String string = objectMapper.writeValueAsString(all);
                String sign = DigestUtils.md5DigestAsHex(string.getBytes());
                Query query = new Query(Criteria.where("sign").is(sign));
                boolean exists = mongoTemplate.exists(query, Map.class,this.collectName);
                if (exists){
                    return;
                }

                all.put("sign", sign);
                long id = snowflake.nextId();
                all.put("id", id);
                all.put("update_time", LocalDateTime.now().format(dateTimeFormatter));
                // update
                if (!ObjectUtils.isEmpty(arrTxt)){
                    Criteria criteria = Criteria.where("1").is(all.get("1"));
                    Arrays.stream(arrTxt).forEach(e->{
                        criteria.and(e).is(all.get(e).toString());
                    });
                    Map one = mongoTemplate.findOne(new Query(criteria), Map.class, this.collectName);
                    if (one != null && !one.isEmpty()){
                        Update update = new Update();
                        all.entrySet().forEach(e->{
                            update.set(e.getKey(),e.getValue());
                        });
                        log.info("更新，信息：{}",update);
                        query = new Query(Criteria.where("sign").is(one.get("sign")));
                        mongoTemplate.updateFirst(query,update,collectName);
                        return;
                    }
                }
                log.info("新增，信息：{}",all);
                mongoTemplate.save(all, collectName);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }
}
