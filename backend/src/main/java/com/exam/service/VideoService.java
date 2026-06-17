package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.entity.Video;
import com.exam.vo.VideoVO;

import java.util.List;

public interface VideoService {

    IPage<VideoVO> page(Integer current, Integer size, String keyword, Long categoryId, String sort, Integer status);

    VideoVO getById(Long id);

    VideoVO getDetailById(Long id);

    boolean save(Video entity);

    boolean updateById(Video entity);

    boolean removeById(Long id);

    boolean incrementViewCount(Long id);

    List<VideoVO> getRelatedVideos(Long videoId, Integer limit);

    List<VideoVO> getHotVideos(Integer limit);
}
