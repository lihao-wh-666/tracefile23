package com.exam.controller;

import com.exam.common.Result;
import com.exam.service.DashboardService;
import com.exam.vo.DashboardVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping
    public Result<DashboardVO> getData() {
        return Result.ok(dashboardService.getData());
    }
}
