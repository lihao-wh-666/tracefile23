package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.annotation.Log;
import com.exam.common.Result;
import com.exam.dto.VideoDTO;
import com.exam.entity.Video;
import com.exam.service.VideoService;
import com.exam.vo.VideoVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private VideoService videoService;

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    @Log(module = "视频管理", operation = "分页查询视频列表", operationType = 4, targetType = "video")
    public Result<IPage<VideoVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) Long categoryId,
                                       @RequestParam(defaultValue = "latest") String sort,
                                       @RequestParam(required = false) Integer status) {
        return Result.ok(videoService.page(current, size, keyword, categoryId, sort, status));
    }

    @GetMapping("/manage-page")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "视频管理", operation = "分页查询视频管理列表", operationType = 4, targetType = "video")
    public Result<IPage<VideoVO>> managePage(@RequestParam(defaultValue = "1") Integer current,
                                             @RequestParam(defaultValue = "10") Integer size,
                                             @RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Long categoryId,
                                             @RequestParam(required = false) Integer status) {
        return Result.ok(videoService.page(current, size, keyword, categoryId, "latest", status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    @Log(module = "视频管理", operation = "查询视频详情", operationType = 4, targetType = "video")
    public Result<VideoVO> getById(@PathVariable Long id) {
        return Result.ok(videoService.getDetailById(id));
    }

    @GetMapping("/info/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "视频管理", operation = "查询视频信息", operationType = 4, targetType = "video")
    public Result<VideoVO> getInfoById(@PathVariable Long id) {
        return Result.ok(videoService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "视频管理", operation = "新增视频", operationType = 1, targetType = "video", recordState = true)
    public Result<Boolean> save(@RequestBody @Valid VideoDTO videoDTO) {
        Video video = new Video();
        video.setTitle(videoDTO.getTitle());
        video.setDescription(videoDTO.getDescription());
        video.setCategoryId(videoDTO.getCategoryId());
        video.setCoverUrl(videoDTO.getCoverUrl());
        video.setVideoUrl(videoDTO.getVideoUrl());
        video.setDuration(videoDTO.getDuration());
        video.setFileSize(videoDTO.getFileSize());
        video.setTags(videoDTO.getTags());
        video.setStatus(videoDTO.getStatus());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.exam.entity.User user) {
            video.setCreateBy(user.getId());
        }

        return Result.ok(videoService.save(video));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "视频管理", operation = "编辑视频", operationType = 2, targetType = "video", recordState = true)
    public Result<Boolean> update(@RequestBody @Valid VideoDTO videoDTO, @RequestParam Long id) {
        Video video = videoService.getById(id) != null ? convertToEntity(videoService.getById(id)) : new Video();
        video.setId(id);
        video.setTitle(videoDTO.getTitle());
        video.setDescription(videoDTO.getDescription());
        video.setCategoryId(videoDTO.getCategoryId());
        video.setCoverUrl(videoDTO.getCoverUrl());
        video.setVideoUrl(videoDTO.getVideoUrl());
        video.setDuration(videoDTO.getDuration());
        video.setFileSize(videoDTO.getFileSize());
        video.setTags(videoDTO.getTags());
        if (videoDTO.getStatus() != null) {
            video.setStatus(videoDTO.getStatus());
        }

        return Result.ok(videoService.updateById(video));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "视频管理", operation = "删除视频", operationType = 3, targetType = "video", recordState = true)
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.ok(videoService.removeById(id));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "视频管理", operation = "上传视频文件", operationType = 1, targetType = "video")
    public Result<String> uploadVideo(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".mp4";
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        File videoDir = new File(uploadPath + File.separator + "video");
        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }

        File destFile = new File(videoDir, fileName);
        file.transferTo(destFile);

        String fileUrl = "/uploads/video/" + fileName;
        return Result.ok(fileUrl);
    }

    @PostMapping("/upload-cover")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "视频管理", operation = "上传视频封面", operationType = 1, targetType = "video")
    public Result<String> uploadCover(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return Result.fail("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

        File coverDir = new File(uploadPath + File.separator + "video" + File.separator + "cover");
        if (!coverDir.exists()) {
            coverDir.mkdirs();
        }

        File destFile = new File(coverDir, fileName);
        file.transferTo(destFile);

        String fileUrl = "/uploads/video/cover/" + fileName;
        return Result.ok(fileUrl);
    }

    @GetMapping("/related/{id}")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    @Log(module = "视频管理", operation = "查询相关视频", operationType = 4, targetType = "video")
    public Result<List<VideoVO>> getRelatedVideos(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "6") Integer limit) {
        return Result.ok(videoService.getRelatedVideos(id, limit));
    }

    @GetMapping("/hot")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    @Log(module = "视频管理", operation = "查询热门视频", operationType = 4, targetType = "video")
    public Result<List<VideoVO>> getHotVideos(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.ok(videoService.getHotVideos(limit));
    }

    private Video convertToEntity(VideoVO vo) {
        Video video = new Video();
        video.setId(vo.getId());
        video.setTitle(vo.getTitle());
        video.setDescription(vo.getDescription());
        video.setCategoryId(vo.getCategoryId());
        video.setCoverUrl(vo.getCoverUrl());
        video.setVideoUrl(vo.getVideoUrl());
        video.setDuration(vo.getDuration());
        video.setFileSize(vo.getFileSize());
        video.setViewCount(vo.getViewCount());
        video.setLikeCount(vo.getLikeCount());
        video.setRating(vo.getRating());
        video.setTags(vo.getTags());
        video.setStatus(vo.getStatus());
        video.setCreateBy(vo.getCreateBy());
        video.setCreateTime(vo.getCreateTime());
        video.setUpdateTime(vo.getUpdateTime());
        video.setDeleted(0);
        return video;
    }
}
