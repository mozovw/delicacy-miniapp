package com.delicacy.support.aspect;

import com.delicacy.support.annotation.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StopWatch;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Slf4j
public class LogAdvice extends AbstractAspect {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.
            ofPattern("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);

    @Override
    @SneakyThrows
    public Object around(ProceedingJoinPoint pjp) {
        Entry processEntry = new Entry(pjp);
        processBefore(processEntry);
        process(processEntry);
        processAfter(processEntry);
        return processEntry.getResult();
    }

    @SneakyThrows
    private void process(Entry processEntry) {
        ProceedingJoinPoint pjp = processEntry.getPjp();
        String classAndMethodName = processEntry.getClassAndMethodName();
        long elapsed;
        Object result;
        try {
            StopWatch stopwatch = new StopWatch();
            stopwatch.start();
            result = pjp.proceed();
            stopwatch.stop();
            elapsed = stopwatch.getTotalTimeMillis();
            processEntry.setResult(result);
            processEntry.setElapsed(elapsed);
        } catch (Throwable e) {
            log.info("【方法执行结束】：{}", classAndMethodName);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String msg = sw.toString();
            log.error("【发生异常，异常简述】：\n{}", msg);
            throw e;
        }
    }

    @Data
    static class Entry {
        String classAndMethodName;
        Method currentMethod;
        long elapsed;
        Object result;
        String className;
        ProceedingJoinPoint pjp;

        Entry(ProceedingJoinPoint pjp) {
            this.pjp = pjp;
        }
    }

    private void processAfter(Entry processEntry) {
        Object result = processEntry.getResult();
        String classAndMethodName = processEntry.getClassAndMethodName();
        Method currentMethod = processEntry.getCurrentMethod();
        long elapsed = processEntry.getElapsed();
        // 处理返回值
        log.info("【方法执行结束】：{}，【结束计时，共计】：{}ms", classAndMethodName, elapsed);
        try {
            if (result != null && currentMethod.isAnnotationPresent(Log.class) && result instanceof Serializable) {
                log.info("【返回结果】:{}", toJSONString(result));
            }
        } catch (Throwable e) {
            log.error("【记录返回日志的时候出错】：{}", e.getMessage());
        }
    }

    private void processBefore(Entry processEntry) {
        ProceedingJoinPoint pjp = processEntry.getPjp();

        String classAndMethodName = null;
        Method currentMethod = null;
        String className = null;
        try {
            MethodSignature mig = (MethodSignature) pjp.getSignature();
            currentMethod = pjp.getTarget().getClass().getMethod(mig.getName(), mig.getParameterTypes());
            className = pjp.getTarget().getClass().getName();
            classAndMethodName = pjp.getTarget().getClass().getName() + "的" + pjp.getSignature().getName() + "方法";
        } catch (Throwable e) {
            log.error("【初始化日志记录异常】，{}", e.getMessage());
        }
        processEntry.setClassName(className);
        processEntry.setCurrentMethod(currentMethod);
        processEntry.setClassAndMethodName(classAndMethodName);
        try {
            ServletRequestAttributes requestAttr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            String format = dateTimeFormatter.format(LocalDateTime.now());
            log.info("【方法执行开始】：{}，【请求IP】：{}，【开始计时】：{}",
                    classAndMethodName, getIpAddr(requestAttr.getRequest()), format);
            if (null == pjp.getArgs() || pjp.getArgs().length == 0) {
                log.info("【方法参数】：{}", "无");
            } else {
                if (pjp.getArgs().length == 1) {
                    if (pjp.getArgs()[0] instanceof Serializable) {
                        if (isFile(pjp.getArgs()[0])) {
                            log.info("【方法参数，文件】：{}", getFileName(pjp.getArgs()[0]));
                        } else {
                            log.info("【方法参数，实体对象】：{}", toJSONString(pjp.getArgs()[0]));
                        }
                    }
                } else {
                    List<Object> list = Arrays.asList(pjp.getArgs());
                    final AtomicInteger index = new AtomicInteger(1);
                    list.stream().filter(x -> x instanceof Serializable).forEach(x -> {
                        if (isFile(x)) {
                            log.info("【方法参数】{}:{}", index.get(), getFileName(x));
                        } else {
                            log.info("【方法参数】{}:{}", index.get(), toJSONString(x));
                        }
                        index.incrementAndGet();
                    });
                }
            }
        } catch (Throwable e) {
            log.error("【记录参数日志异常】：{}", e.getMessage());
        }
    }

    private String getFileName(Object file) {
        if (file == null) {
            return "空文件";
        } else if (file instanceof MultipartFile) {
            return ((MultipartFile) file).getOriginalFilename();
        } else if (file instanceof MultipartFile[]) {
            return Arrays.stream(((MultipartFile[]) file)).map(MultipartFile::getOriginalFilename).collect(Collectors.joining(","));
        }
        return "空文件";
    }

    private String toJSONString(Object json) {
        try {
            return objectMapper.writeValueAsString(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getIpAddr(HttpServletRequest request) {
        String ipAddress;
        try {
            ipAddress = request.getHeader("x-forwarded-for");
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
                if ("127.0.0.1".equals(ipAddress)) {
                    // 根据网卡取本机配置的IP
                    InetAddress inet = null;
                    try {
                        inet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        log.error("【获取IP异常】：{}", e.getMessage());
                        e.printStackTrace();
                    }
                    assert inet != null;
                    ipAddress = inet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
            if (ipAddress != null && ipAddress.length() > 15) { // "***.***.***.***".length()
                // = 15
                if (ipAddress.indexOf(",") > 0) {
                    ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
                }
            }
        } catch (Exception e) {
            ipAddress = "";
        }
        return ipAddress;
    }


}
