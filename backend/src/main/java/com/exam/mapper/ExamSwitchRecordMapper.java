package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.ExamSwitchRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ExamSwitchRecordMapper extends BaseMapper<ExamSwitchRecord> {

    @Select("SELECT * FROM exam_switch_record WHERE record_id = #{recordId} ORDER BY switch_time DESC")
    List<ExamSwitchRecord> selectByRecordId(@Param("recordId") Long recordId);

    @Select("SELECT COUNT(*) FROM exam_switch_record WHERE record_id = #{recordId} AND switch_type = #{switchType}")
    Integer countByRecordIdAndType(@Param("recordId") Long recordId, @Param("switchType") Integer switchType);

    @Select("SELECT COALESCE(SUM(duration), 0) FROM exam_switch_record WHERE record_id = #{recordId}")
    Integer sumDurationByRecordId(@Param("recordId") Long recordId);
}
