package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.LogStoragePolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LogStoragePolicyMapper extends BaseMapper<LogStoragePolicy> {

    @Select("SELECT * FROM log_storage_policy ORDER BY id ASC LIMIT 1")
    LogStoragePolicy selectActivePolicy();
}
