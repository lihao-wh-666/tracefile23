package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.VideoCategory;
import com.exam.mapper.VideoCategoryMapper;
import com.exam.service.VideoCategoryService;
import com.exam.vo.VideoCategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoCategoryServiceImpl implements VideoCategoryService {

    @Autowired
    private VideoCategoryMapper videoCategoryMapper;

    @Override
    public IPage<VideoCategoryVO> page(Integer current, Integer size, String name) {
        Page<VideoCategory> page = new Page<>(current, size);
        LambdaQueryWrapper<VideoCategory> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(VideoCategory::getName, name);
        }
        wrapper.orderByAsc(VideoCategory::getSort);
        wrapper.orderByDesc(VideoCategory::getCreateTime);
        IPage<VideoCategory> categoryPage = videoCategoryMapper.selectPage(page, wrapper);
        return convertToVOPage(categoryPage);
    }

    @Override
    public List<VideoCategoryVO> list() {
        LambdaQueryWrapper<VideoCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VideoCategory::getStatus, 1);
        wrapper.orderByAsc(VideoCategory::getSort);
        wrapper.orderByDesc(VideoCategory::getCreateTime);
        List<VideoCategory> list = videoCategoryMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public VideoCategoryVO getById(Long id) {
        VideoCategory category = videoCategoryMapper.selectById(id);
        return convertToVO(category);
    }

    @Override
    public boolean save(VideoCategory entity) {
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        return videoCategoryMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(VideoCategory entity) {
        return videoCategoryMapper.updateById(entity) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return videoCategoryMapper.deleteById(id) > 0;
    }

    private VideoCategoryVO convertToVO(VideoCategory entity) {
        if (entity == null) {
            return null;
        }
        VideoCategoryVO vo = new VideoCategoryVO();
        BeanUtils.copyProperties(entity, vo);
        vo.setStatusName(entity.getStatus() == 1 ? "启用" : "禁用");
        return vo;
    }

    private IPage<VideoCategoryVO> convertToVOPage(IPage<VideoCategory> page) {
        Page<VideoCategoryVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<VideoCategoryVO> voList = page.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }
}
