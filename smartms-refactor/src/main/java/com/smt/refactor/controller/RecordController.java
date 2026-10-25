package com.smt.refactor.controller;

import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.MissCallQuery;
import com.smt.refactor.dto.RecordQuery;
import com.smt.refactor.dto.RecordVO;
import com.smt.refactor.service.RecordService;
import com.smt.refactor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 交互查询接口（翻译自老系统 record 包 RecordController + MissedCallController）
 */
@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class RecordController {

    private final RecordService recordService;
    private final UserService userService;

    /** 当前登录坐席工号（qualityAgCode） */
    private String myAgentCode(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof Integer)) {
            throw new BusinessException("未登录");
        }
        return userService.agentCodeOf((Integer) auth.getPrincipal());
    }

    /** 通话详单（agentId="my"=我的通话） */
    @GetMapping
    public PageResult<RecordVO> callRecords(RecordQuery query, Authentication auth) {
        return recordService.callRecords(query, myAgentCode(auth));
    }

    /** 未接记录（type=1坐席未接 2队列未接 3客户未接 4留言；agentId="my"=我的未接） */
    @GetMapping("/missed")
    public PageResult<RecordVO> missedCalls(MissCallQuery query, Authentication auth) {
        return recordService.missedCalls(query, myAgentCode(auth));
    }
}
