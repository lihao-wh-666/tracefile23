package com.exam.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.exam.entity.OperationLogArchive;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OperationLogArchiveMapper extends BaseMapper<OperationLogArchive> {

    @Select("SELECT checksum FROM operation_log_archive ORDER BY id DESC LIMIT 1")
    String selectLastChecksum();

    @Select("SELECT id, checksum, previous_checksum FROM operation_log_archive WHERE id >= #{startId} AND id <= #{endId} ORDER BY id ASC")
    List<Map<String, Object>> selectChecksumChain(Long startId, Long endId);

    @Select("SELECT id, operation_type, target_type, target_id, before_state, after_state, user_agent, trace_id, checksum, previous_checksum FROM operation_log_archive WHERE id = #{id}")
    OperationLogArchive selectDetailWithExt(Long id);

    @Select("SELECT COUNT(*) FROM operation_log_archive WHERE create_time >= #{startTime} AND create_time < #{endTime} AND storage_level = 2")
    Long countByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    IPage<OperationLogArchive> selectPageByCondition(IPage<OperationLogArchive> page, @Param(Constants.WRAPPER) Wrapper<OperationLogArchive> wrapper);

    @Select("SELECT * FROM operation_log_archive WHERE create_time >= #{startTime} AND create_time < #{endTime} AND storage_level = 2 ORDER BY id ASC LIMIT #{offset}, #{limit}")
    List<OperationLogArchive> selectForFileExport(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime,
                                                   @Param("offset") int offset,
                                                   @Param("limit") int limit);
}
