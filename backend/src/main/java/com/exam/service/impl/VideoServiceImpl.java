package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.Video;
import com.exam.entity.VideoCategory;
import com.exam.mapper.VideoCategoryMapper;
import com.exam.mapper.VideoMapper;
import com.exam.service.VideoService;
import com.exam.vo.VideoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl implements VideoService {

    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private VideoCategoryMapper videoCategoryMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public IPage<VideoVO> page(Integer current, Integer size, String keyword, Long categoryId, String sort, Integer status) {
        Page<Video> page = new Page<>(current, size);
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Video::getTitle, keyword)
                    .or().like(Video::getDescription, keyword)
                    .or().like(Video::getTags, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Video::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Video::getStatus, status);
        } else {
            wrapper.eq(Video::getStatus, 1);
        }

        if ("hot".equals(sort)) {
            wrapper.orderByDesc(Video::getViewCount);
        } else if ("rating".equals(sort)) {
            wrapper.orderByDesc(Video::getRating);
        } else {
            wrapper.orderByDesc(Video::getCreateTime);
        }

        IPage<Video> videoPage = videoMapper.selectPage(page, wrapper);
        return convertToVOPage(videoPage);
    }

    @Override
    public VideoVO getById(Long id) {
        Video video = videoMapper.selectById(id);
        return convertToVO(video);
    }

    @Override
    public VideoVO getDetailById(Long id) {
        Video video = videoMapper.selectVideoWithCategory(id);
        if (video != null) {
            incrementViewCount(id);
        }
        return convertToVO(video);
    }

    @Override
    public boolean save(Video entity) {
        if (entity.getViewCount() == null) {
            entity.setViewCount(0);
        }
        if (entity.getLikeCount() == null) {
            entity.setLikeCount(0);
        }
        if (entity.getRating() == null) {
            entity.setRating(BigDecimal.ZERO);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(0);
        }
        return videoMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(Video entity) {
        return videoMapper.updateById(entity) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return videoMapper.deleteById(id) > 0;
    }

    @Override
    public boolean incrementViewCount(Long id) {
        Video video = videoMapper.selectById(id);
        if (video != null) {
            video.setViewCount(video.getViewCount() + 1);
            return videoMapper.updateById(video) > 0;
        }
        return false;
    }

    @Override
    public List<VideoVO> getRelatedVideos(Long videoId, Integer limit) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) {
            return List.of();
        }

        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getCategoryId, video.getCategoryId());
        wrapper.ne(Video::getId, videoId);
        wrapper.eq(Video::getStatus, 1);
        wrapper.orderByDesc(Video::getViewCount);
        wrapper.last("LIMIT " + limit);

        List<Video> videos = videoMapper.selectList(wrapper);
        return videos.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<VideoVO> getHotVideos(Integer limit) {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getStatus, 1);
        wrapper.orderByDesc(Video::getViewCount);
        wrapper.last("LIMIT " + limit);

        List<Video> videos = videoMapper.selectList(wrapper);
        return videos.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private VideoVO convertToVO(Video entity) {
        if (entity == null) {
            return null;
        }
        VideoVO vo = new VideoVO();
        BeanUtils.copyProperties(entity, vo);

        if (entity.getTags() != null && !entity.getTags().isEmpty()) {
            vo.setTagList(Arrays.asList(entity.getTags().split(",")));
        }

        if (entity.getCreateTime() != null) {
            vo.setPublishDate(entity.getCreateTime().format(DATE_FORMATTER));
        }

        String statusName = switch (entity.getStatus()) {
            case 0 -> "草稿";
            case 1 -> "已发布";
            case 2 -> "已下架";
            default -> "未知";
        };
        vo.setStatusName(statusName);

        if (vo.getCategoryName() == null && entity.getCategoryId() != null) {
            VideoCategory category = videoCategoryMapper.selectById(entity.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        return vo;
    }

    private IPage<VideoVO> convertToVOPage(IPage<Video> page) {
        Page<VideoVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());

        List<Long> categoryIds = page.getRecords().stream()
                .map(Video::getCategoryId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> categoryNameMap = videoCategoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(VideoCategory::getId, VideoCategory::getName));

        List<VideoVO> voList = page.getRecords().stream()
                .map(video -> {
                    VideoVO vo = convertToVO(video);
                    if (vo != null && vo.getCategoryName() == null) {
                        vo.setCategoryName(categoryNameMap.get(video.getCategoryId()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }
}
