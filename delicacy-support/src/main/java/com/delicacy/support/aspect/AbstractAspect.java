package com.delicacy.support.aspect;

import com.delicacy.common.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;


public abstract class AbstractAspect implements MethodInterceptor {

    protected ObjectMapper objectMapper = new ObjectMapper();

    protected abstract Object around(ProceedingJoinPoint pjp);

    @Override
    public Object invoke(MethodInvocation mi) {
        if (!(mi instanceof ProxyMethodInvocation)) {
            throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
        }
        ProxyMethodInvocation pmi = (ProxyMethodInvocation) mi;
        ProceedingJoinPoint pjp = new MethodInvocationProceedingJoinPoint(pmi);
        return around(pjp);
    }

    protected void preHandle(ProceedingJoinPoint pjp, Optional<Map<String, Object>> optional) throws IllegalAccessException {
        if (pjp.getArgs().length == 1) {
            Object arg = pjp.getArgs()[0];
            if (arg instanceof Serializable && !ObjectUtils.isBasicType(arg) && !(arg instanceof String)) {
                if (!isFile(arg)) {
                    Class<?> aClass = arg.getClass();
                    Field[] declaredFields = aClass.getDeclaredFields();
                    for (int i = 0; i < declaredFields.length; i++) {
                        Field field = declaredFields[i];
                        Map<String, Object> service = optional.get();
                        Iterator<Map.Entry<String, Object>> iterator = service.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<String, Object> e = iterator.next();
                            if (e.getKey().equalsIgnoreCase(field.getName())) {
                                field.setAccessible(true);
                                Object o = field.get(arg);
                                if (o != null) {
                                    continue;
                                }
                                field.set(arg, e.getValue());
                            }
                        }
                    }

                }
            }
        }
    }


    protected boolean isFile(Object obj) {
        return obj instanceof MultipartFile || obj instanceof MultipartFile[];
    }
}
