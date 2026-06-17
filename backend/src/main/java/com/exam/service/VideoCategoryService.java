package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.entity.VideoCategory;
import com.exam.vo.VideoCategoryVO;

import java.util.List;

public interface VideoCategoryService {

    IPage<VideoCategoryVO> page(Integer current, Integer size, String name);

    List<VideoCategoryVO> list();

    VideoCategoryVO getById(Long id);

    boolean save(VideoCategory entity);

    boolean updateById(VideoCategory entity);

    boolean removeById(Long id);
}
