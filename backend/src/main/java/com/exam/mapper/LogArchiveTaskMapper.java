package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.LogArchiveTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface LogArchiveTaskMapper extends BaseMapper<LogArchiveTask> {

    @Select("SELECT * FROM log_archive_task WHERE status IN (0, 1) ORDER BY create_time ASC LIMIT 1")
    LogArchiveTask selectNextPendingTask();

    @Select("SELECT * FROM log_archive_task WHERE task_type = #{taskType} AND status = 2 ORDER BY execute_end_time DESC LIMIT 1")
    LogArchiveTask selectLastSuccessTask(@Param("taskType") Integer taskType);

    @Update("UPDATE log_archive_task SET status = 1, execute_start_time = #{executeStartTime} WHERE id = #{id} AND status IN (0, 1)")
    int markTaskRunning(@Param("id") Long id, @Param("executeStartTime") LocalDateTime executeStartTime);

    @Update("UPDATE log_archive_task SET status = #{status}, success_count = #{successCount}, " +
            "fail_count = #{failCount}, error_msg = #{errorMsg}, execute_end_time = #{executeEndTime}, " +
            "file_path = #{filePath}, file_checksum = #{fileChecksum}, file_size = #{fileSize} WHERE id = #{id}")
    int updateTaskResult(@Param("id") Long id,
                         @Param("status") Integer status,
                         @Param("successCount") Long successCount,
                         @Param("failCount") Long failCount,
                         @Param("errorMsg") String errorMsg,
                         @Param("executeEndTime") LocalDateTime executeEndTime,
                         @Param("filePath") String filePath,
                         @Param("fileChecksum") String fileChecksum,
                         @Param("fileSize") Long fileSize);

    @Select("SELECT * FROM log_archive_task WHERE status = 2 ORDER BY create_time DESC LIMIT #{limit}")
    List<LogArchiveTask> selectRecentSuccessTasks(@Param("limit") Integer limit);
}
