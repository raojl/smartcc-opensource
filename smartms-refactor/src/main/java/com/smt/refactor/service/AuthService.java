package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.smt.refactor.dto.LoginResponse;
import com.smt.refactor.dto.UserInfoVO;
import com.smt.refactor.entity.SysRole;
import com.smt.refactor.entity.SysUser;
import com.smt.refactor.entity.SysUserrole;
import com.smt.refactor.mapper.SysRoleMapper;
import com.smt.refactor.mapper.SysUserMapper;
import com.smt.refactor.mapper.SysUserroleMapper;
import com.smt.refactor.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务：登录校验（兼容存量 MD5）、用户信息
 */
@Service
@RequiredArgsConstructor
@DS("conf")
public class AuthService {

    private final SysUserMapper sysUserMapper;
    private final SysUserroleMapper sysUserroleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(String username, String password) {
        SysUser user = sysUserMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysUser>()
                        .eq("login_name", username));
        if (user == null) {
            throw new com.smt.refactor.common.BusinessException("用户名或密码错误");
        }
        // 密码校验：32 位 hex 视为存量 MD5（兼容老系统），否则按 BCrypt
        boolean matched;
        String stored = user.getPassword();
        if (stored != null && stored.matches("[0-9a-fA-F]{32}")) {
            matched = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8))
                    .equalsIgnoreCase(stored);
        } else {
            matched = passwordEncoder.matches(password, stored);
        }
        if (!matched) {
            throw new com.smt.refactor.common.BusinessException("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getUserId(), user.getLoginName());
        return new LoginResponse(token, buildUserInfo(user));
    }

    public UserInfoVO me(Integer userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new com.smt.refactor.common.BusinessException("用户不存在");
        }
        return buildUserInfo(user);
    }

    private UserInfoVO buildUserInfo(SysUser user) {
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUserName(user.getUserName());
        vo.setLoginName(user.getLoginName());
        vo.setTelephone(user.getTelephone());
        vo.setEmail(user.getEmail());
        vo.setRoleType(user.getRoleType() != null ? user.getRoleType().intValue() : null);
        vo.setQualityAgCode(user.getQualityAgCode());
        vo.setQualityAgDN(user.getQualityAgDN());
        // 角色名列表
        List<Integer> roleIds = sysUserroleMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SysUserrole>()
                                .eq("user_id", user.getUserId()))
                .stream().map(SysUserrole::getRoleId).collect(Collectors.toList());
        List<String> roles = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            roles = sysRoleMapper.selectBatchIds(roleIds).stream()
                    .map(SysRole::getRoleName).collect(Collectors.toList());
        }
        vo.setRoles(roles);
        return vo;
    }
}
