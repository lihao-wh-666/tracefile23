package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.entity.SystemConfig;

import java.util.List;

public interface SystemConfigService {

    IPage<SystemConfig> page(Integer current, Integer size, String configKey, String configName);

    List<SystemConfig> list();

    SystemConfig getById(Long id);

    SystemConfig getByKey(String configKey);

    String getValueByKey(String configKey);

    Integer getIntValueByKey(String configKey, Integer defaultValue);

    boolean save(SystemConfig entity);

    boolean updateById(SystemConfig entity);

    boolean updateByKey(String configKey, String configValue);

    boolean removeById(Long id);

    void refreshCache();
}
