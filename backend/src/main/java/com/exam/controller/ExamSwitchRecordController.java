package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.Result;
import com.exam.dto.ExamSwitchRecordDTO;
import com.exam.entity.ExamSwitchRecord;
import com.exam.service.ExamSwitchRecordService;
import com.exam.vo.ExamSwitchStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/switch-record")
public class ExamSwitchRecordController {

    @Autowired
    private ExamSwitchRecordService switchRecordService;

    private Long getCurrentUserId() {
        return Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    private Integer getCurrentUserRole() {
        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (authorities != null) {
            for (var authority : authorities) {
                String auth = authority.getAuthority();
                if (auth != null && auth.startsWith("ROLE_")) {
                    try {
                        return Integer.parseInt(auth.substring(5));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return null;
    }

    @PostMapping("/record")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<Boolean> recordSwitch(@RequestBody @Valid ExamSwitchRecordDTO dto) {
        Long userId = getCurrentUserId();
        return Result.ok(switchRecordService.recordSwitch(dto, userId));
    }

    @PostMapping("/warning")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<Boolean> incrementWarningCount(@RequestParam Long recordId) {
        Long userId = getCurrentUserId();
        return Result.ok(switchRecordService.incrementWarningCount(recordId, userId));
    }

    @GetMapping("/{recordId}/list")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<List<ExamSwitchRecord>> getByRecordId(@PathVariable Long recordId) {
        Long userId = getCurrentUserId();
        Integer userRole = getCurrentUserRole();
        return Result.ok(switchRecordService.getByRecordId(recordId, userId, userRole));
    }

    @GetMapping("/{recordId}/statistics")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<ExamSwitchStatisticsVO> getStatistics(@PathVariable Long recordId) {
        Long userId = getCurrentUserId();
        Integer userRole = getCurrentUserRole();
        return Result.ok(switchRecordService.getStatistics(recordId, userId, userRole));
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('1', '2')")
    public Result<IPage<ExamSwitchRecord>> page(@RequestParam(defaultValue = "1") Integer current,
                                                @RequestParam(defaultValue = "10") Integer size,
                                                @RequestParam(required = false) Long recordId,
                                                @RequestParam(required = false) Long userId) {
        return Result.ok(switchRecordService.page(current, size, recordId, userId));
    }
}
