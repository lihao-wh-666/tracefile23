package com.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.exam.entity.Video;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {

    @Select("SELECT v.*, vc.name as category_name FROM video v LEFT JOIN video_category vc ON v.category_id = vc.id WHERE v.id = #{id}")
    Video selectVideoWithCategory(@Param("id") Long id);
}
