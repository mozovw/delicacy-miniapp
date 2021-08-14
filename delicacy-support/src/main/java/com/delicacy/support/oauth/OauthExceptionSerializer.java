package com.delicacy.support.oauth;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.*;

public class OauthExceptionSerializer extends StdSerializer<OauthException> {
    private static final long serialVersionUID = 2652127645704345563L;

    public OauthExceptionSerializer() {
        super(OauthException.class);
    }

    @Override
    public void serialize(OauthException value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        //HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        gen.writeObjectField("success",false);
        List<Map<String,String>> list = new ArrayList<>();
        Map<String,String> errorMap = new LinkedHashMap<>();
        errorMap.put("error",value.getOAuth2ErrorCode());
        errorMap.put("message",value.getMessage());
        list.add(errorMap);
        gen.writeObjectField("errors",list);
        if (value.getAdditionalInformation()!=null) {
            for (Map.Entry<String, String> entry : value.getAdditionalInformation().entrySet()) {
                String key = entry.getKey();
                String add = entry.getValue();
                gen.writeObjectField(key, add);
            }
        }
        gen.writeEndObject();
    }
}
