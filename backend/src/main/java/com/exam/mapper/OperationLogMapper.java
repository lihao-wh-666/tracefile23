package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    @Select("SELECT checksum FROM operation_log ORDER BY id DESC LIMIT 1")
    String selectLastChecksum();

    @Select("SELECT id, checksum, previous_checksum FROM operation_log WHERE id >= #{startId} AND id <= #{endId} ORDER BY id ASC")
    List<Map<String, Object>> selectChecksumChain(Long startId, Long endId);

    @Select("SELECT operation_type, COUNT(*) as cnt FROM operation_log WHERE create_time >= #{startTime} AND create_time <= #{endTime} GROUP BY operation_type")
    List<Map<String, Object>> selectOperationTypeStats(String startTime, String endTime);

    @Select("SELECT module, COUNT(*) as cnt FROM operation_log WHERE create_time >= #{startTime} AND create_time <= #{endTime} GROUP BY module ORDER BY cnt DESC LIMIT 10")
    List<Map<String, Object>> selectModuleStats(String startTime, String endTime);

    @Select("SELECT username, COUNT(*) as cnt FROM operation_log WHERE create_time >= #{startTime} AND create_time <= #{endTime} AND username IS NOT NULL GROUP BY username ORDER BY cnt DESC LIMIT 10")
    List<Map<String, Object>> selectUserStats(String startTime, String endTime);

    @Select("SELECT DATE(create_time) as date, COUNT(*) as cnt FROM operation_log WHERE create_time >= #{startTime} AND create_time <= #{endTime} GROUP BY DATE(create_time) ORDER BY date ASC")
    List<Map<String, Object>> selectDateStats(String startTime, String endTime);

    @Select("SELECT status, COUNT(*) as cnt FROM operation_log WHERE create_time >= #{startTime} AND create_time <= #{endTime} GROUP BY status")
    List<Map<String, Object>> selectStatusStats(String startTime, String endTime);

    @Select("SELECT id, operation_type, target_type, target_id, before_state, after_state, user_agent, trace_id, checksum, previous_checksum FROM operation_log WHERE id = #{id}")
    OperationLog selectDetailWithExt(Long id);
}
