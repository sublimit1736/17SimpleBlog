package cn.chunana.simblog17api.config;

import cn.chunana.simblog17api.common.CacheNames;
import cn.chunana.simblog17api.common.CacheTtls;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    // ----------------------------------------------------------- ObjectMapper

    /**
     * 专用于 Redis 序列化的 ObjectMapper。
     * 开启默认类型信息写入（@class）以支持泛型反序列化。
     */
    @Bean("redisObjectMapper")
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 写入 @class 字段，保证泛型类型可正确反序列化
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    /**
     * 通用 JSON 序列化器（序列化为 Object，携带 @class 类型信息）。
     * 取代已弃用的 GenericJackson2JsonRedisSerializer。
     */
    private Jackson2JsonRedisSerializer<Object> jsonSerializer(ObjectMapper redisObjectMapper) {
        return new Jackson2JsonRedisSerializer<>(redisObjectMapper, Object.class);
    }

    // ------------------------------------------------------- RedisTemplate

    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        Jackson2JsonRedisSerializer<Object> jsonSer = jsonSerializer(redisObjectMapper);

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSer);
        template.setHashValueSerializer(jsonSer);
        template.afterPropertiesSet();
        return template;
    }

    // ------------------------------------------------------- CacheManager

    /**
     * 各缓存名称及其 TTL：
     * <ul>
     *   <li>{@code home:latest}          – 最新文章列表，60 s</li>
     *   <li>{@code home:hot}             – 热门文章列表，5 min</li>
     *   <li>{@code home:stats}           – 站点统计，5 min</li>
     *   <li>{@code home:hot-tags}        – 热门标签，10 min</li>
     *   <li>{@code home:recent-comments} – 最新评论，60 s</li>
     * </ul>
     */
    @Bean
    public RedisCacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper redisObjectMapper) {

        Jackson2JsonRedisSerializer<Object> jsonSer = jsonSerializer(redisObjectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();

        // 最新文章 — 内容更新较快，较短 TTL
        cacheConfigs.put(CacheNames.HOME_LATEST,
                defaultConfig.entryTtl(CacheTtls.HOME_LATEST));

        // 热门文章 — 浏览量变化不需要实时，稍长 TTL
        cacheConfigs.put(CacheNames.HOME_HOT,
                defaultConfig.entryTtl(CacheTtls.HOME_HOT));

        // 站点统计
        cacheConfigs.put(CacheNames.HOME_STATS,
                defaultConfig.entryTtl(CacheTtls.HOME_STATS));

        // 热门标签 — 变化最慢
        cacheConfigs.put(CacheNames.HOME_HOT_TAGS,
                defaultConfig.entryTtl(CacheTtls.HOME_HOT_TAGS));

        // 最新评论 — 更新较快
        cacheConfigs.put(CacheNames.HOME_RECENT_COMMENTS,
                defaultConfig.entryTtl(CacheTtls.HOME_RECENT_COMMENTS));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig.entryTtl(CacheTtls.DEFAULT))
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}


