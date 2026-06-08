package com.exam.controller;

import com.exam.common.Result;
import com.exam.service.DashboardService;
import com.exam.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<DashboardVO> getData() {
        return Result.ok(dashboardService.getData());
    }
}
