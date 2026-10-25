package com.smt.refactor.controller;

import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.ChangePasswordRequest;
import com.smt.refactor.dto.UserSaveRequest;
import com.smt.refactor.dto.UserVO;
import com.smt.refactor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理接口（翻译自老系统 /usermanager）
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 分页列表（user_name 模糊） */
    @GetMapping
    public PageResult<UserVO> page(@RequestParam(defaultValue = "1") long page,
                                   @RequestParam(defaultValue = "15") long size,
                                   @RequestParam(required = false) String username) {
        return userService.page(page, size, username);
    }

    /** 详情（含角色） */
    @GetMapping("/{id}")
    public UserVO detail(@PathVariable Integer id) {
        return userService.detail(id);
    }

    /** 新增 */
    @PostMapping
    public Map<String, Object> create(@Valid @RequestBody UserSaveRequest req) {
        Integer id = userService.create(req);
        Map<String, Object> r = new HashMap<>();
        r.put("userId", id);
        return r;
    }

    /** 编辑 */
    @PutMapping("/{id}")
    public void update(@PathVariable Integer id, @Valid @RequestBody UserSaveRequest req) {
        userService.update(id, req);
    }

    /** 删除（签入中禁止） */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        userService.delete(id);
    }

    /** 重置密码为登录名 MD5 */
    @PostMapping("/{id}/reset-password")
    public void resetPassword(@PathVariable Integer id) {
        userService.resetPassword(id);
    }

    /** 当前用户修改自己的密码 */
    @PutMapping("/me/password")
    public void changePassword(Authentication authentication,
                               @Valid @RequestBody ChangePasswordRequest req) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Integer)) {
            throw new com.smt.refactor.common.BusinessException("未登录");
        }
        userService.changePassword((Integer) authentication.getPrincipal(), req);
    }

    /** 登录名唯一性校验（老系统 checkLoginName） */
    @GetMapping("/check-login-name")
    public Map<String, Boolean> checkLoginName(@RequestParam String loginName,
                                               @RequestParam(required = false) Integer excludeId) {
        Map<String, Boolean> r = new HashMap<>();
        r.put("available", userService.isLoginNameAvailable(loginName, excludeId));
        return r;
    }

    /** 坐席工号唯一性校验（老系统 checkAgentId） */
    @GetMapping("/check-agent-code")
    public Map<String, Boolean> checkAgentCode(@RequestParam String qualityAgCode,
                                               @RequestParam(required = false) Integer excludeId) {
        Map<String, Boolean> r = new HashMap<>();
        r.put("available", userService.isAgentCodeAvailable(qualityAgCode, excludeId));
        return r;
    }
}
