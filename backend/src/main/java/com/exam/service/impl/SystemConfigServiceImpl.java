package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.SystemConfig;
import com.exam.mapper.SystemConfigMapper;
import com.exam.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    private static final String REDIS_KEY_PREFIX = "system:config:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    private final Map<String, SystemConfig> localCache = new ConcurrentHashMap<>();

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void init() {
        refreshCache();
    }

    @Override
    public IPage<SystemConfig> page(Integer current, Integer size, String configKey, String configName) {
        Page<SystemConfig> page = new Page<>(current, size);
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        if (configKey != null && !configKey.isEmpty()) {
            wrapper.like(SystemConfig::getConfigKey, configKey);
        }
        if (configName != null && !configName.isEmpty()) {
            wrapper.like(SystemConfig::getConfigName, configName);
        }
        wrapper.orderByDesc(SystemConfig::getCreateTime);
        return systemConfigMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SystemConfig> list() {
        return systemConfigMapper.selectList(null);
    }

    @Override
    public SystemConfig getById(Long id) {
        return systemConfigMapper.selectById(id);
    }

    @Override
    public SystemConfig getByKey(String configKey) {
        SystemConfig config = localCache.get(configKey);
        if (config != null) {
            return config;
        }

        String redisKey = REDIS_KEY_PREFIX + configKey;
        config = (SystemConfig) redisTemplate.opsForValue().get(redisKey);
        if (config != null) {
            localCache.put(configKey, config);
            return config;
        }

        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        config = systemConfigMapper.selectOne(wrapper);
        if (config != null) {
            redisTemplate.opsForValue().set(redisKey, config, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            localCache.put(configKey, config);
        }
        return config;
    }

    @Override
    public String getValueByKey(String configKey) {
        SystemConfig config = getByKey(configKey);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public Integer getIntValueByKey(String configKey, Integer defaultValue) {
        String value = getValueByKey(configKey);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public boolean save(SystemConfig entity) {
        int result = systemConfigMapper.insert(entity);
        if (result > 0) {
            evictCache(entity.getConfigKey());
        }
        return result > 0;
    }

    @Override
    public boolean updateById(SystemConfig entity) {
        SystemConfig oldConfig = systemConfigMapper.selectById(entity.getId());
        int result = systemConfigMapper.updateById(entity);
        if (result > 0 && oldConfig != null) {
            evictCache(oldConfig.getConfigKey());
            if (!oldConfig.getConfigKey().equals(entity.getConfigKey())) {
                evictCache(entity.getConfigKey());
            }
        }
        return result > 0;
    }

    @Override
    public boolean updateByKey(String configKey, String configValue) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        if (config == null) {
            return false;
        }
        config.setConfigValue(configValue);
        int result = systemConfigMapper.updateById(config);
        if (result > 0) {
            evictCache(configKey);
        }
        return result > 0;
    }

    @Override
    public boolean removeById(Long id) {
        SystemConfig config = systemConfigMapper.selectById(id);
        int result = systemConfigMapper.deleteById(id);
        if (result > 0 && config != null) {
            evictCache(config.getConfigKey());
        }
        return result > 0;
    }

    @Override
    public void refreshCache() {
        localCache.clear();
        List<SystemConfig> allConfigs = systemConfigMapper.selectList(null);
        for (SystemConfig config : allConfigs) {
            String redisKey = REDIS_KEY_PREFIX + config.getConfigKey();
            redisTemplate.opsForValue().set(redisKey, config, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            localCache.put(config.getConfigKey(), config);
        }
    }

    private void evictCache(String configKey) {
        localCache.remove(configKey);
        redisTemplate.delete(REDIS_KEY_PREFIX + configKey);
    }
}
