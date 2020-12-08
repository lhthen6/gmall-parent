package com.atguigu.gmall.config;

import com.atguigu.gmall.common.constant.RedisConst;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    //通知
    @Around("@annotation(com.atguigu.gmall.config.GmallCache)")//切入点
    public Object cacheAroundAdvice(ProceedingJoinPoint point) {// 连接点
        Object result = null;
        String cacheKey = "";
        //获得方法信息
        MethodSignature signature = (MethodSignature) point.getSignature();
        //获得方法名和对应的缓存前后缀
        String name = signature.getMethod().getName();
        cacheKey = name;
        //获得方法返回值类型
        Class returnType = signature.getReturnType();
        //获得方法参数
        Object[] args = point.getArgs();
        for (Object arg : args) {
            cacheKey = cacheKey + ":" + arg;
        }
        //注解信息
        GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);

        //查询缓存
        result = redisTemplate.opsForValue().get(cacheKey);
        //缓存没有
        if (null == result) {
            try {
                //分布式锁
                String key = UUID.randomUUID().toString();
                Boolean OK = redisTemplate.opsForValue().setIfAbsent(cacheKey + ":lock", key, 3, TimeUnit.SECONDS);
                if (OK) {
                    // 执行被代理方法
                    result = point.proceed();
                    if (null == result) {
                        //同步空缓存
                        redisTemplate.opsForValue().set(cacheKey, result, 5, TimeUnit.SECONDS);
                    } else {
                        //同步缓存
                        redisTemplate.opsForValue().set(cacheKey, result);
                        // 释放锁方法一
                        String openKey = (String) redisTemplate.opsForValue().get(cacheKey + ":lock");
                        if (key.equals(openKey)) {
                            redisTemplate.delete(cacheKey + ":lock");
                        }
//                        // 释放锁方法二
//                        // 解锁：使用lua 脚本解锁
//                        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//                        // 设置lua脚本返回的数据类型
//                        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//                        // 设置lua脚本返回类型为Long
//                        redisScript.setResultType(Long.class);
//                        redisScript.setScriptText(script);
//                        // 删除key 所对应的 value
//                        redisTemplate.execute(redisScript, Arrays.asList("sku:" + skuId + ":lock"), key);
                    }
                } else {
                    // 自旋
                    return redisTemplate.opsForValue().get(cacheKey);
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
        return result;
    }

}
