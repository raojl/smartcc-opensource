package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smt.refactor.common.BusinessException;
import com.smt.refactor.common.PageResult;
import com.smt.refactor.dto.ChangePasswordRequest;
import com.smt.refactor.dto.UserSaveRequest;
import com.smt.refactor.dto.UserVO;
import com.smt.refactor.entity.SysRole;
import com.smt.refactor.entity.SysUser;
import com.smt.refactor.entity.SysUserrole;
import com.smt.refactor.mapper.SysRoleMapper;
import com.smt.refactor.mapper.SysUserMapper;
import com.smt.refactor.mapper.SysUserroleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务：CRUD + 角色绑定 + 密码管理
 * 翻译自老系统 UserController（sysmanager 包）
 */
@Slf4j
@Service
@RequiredArgsConstructor
@DS("conf")
public class UserService {

    /** CTI 实时监控地址（application.yml: cti.monitor-server），坐席签入校验用 */
    @org.springframework.beans.factory.annotation.Value("${cti.monitor-server:http://127.0.0.1:8091}")
    private String ctiMonitorServer;

    private final SysUserMapper sysUserMapper;
    private final SysUserroleMapper sysUserroleMapper;
    private final SysRoleMapper sysRoleMapper;

    /**
     * 分页查询用户列表（user_name 模糊匹配，与老系统 index 一致）
     */
    public PageResult<UserVO> page(long pageNo, long pageSize, String username) {
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        if (StringUtils.hasText(username)) {
            qw.like("user_name", username.trim());
        }
        qw.orderByDesc("user_id");
        Page<SysUser> p = sysUserMapper.selectPage(new Page<>(pageNo, pageSize), qw);
        List<UserVO> list = p.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(list, p.getTotal(), p.getCurrent(), p.getSize(), p.getPages());
    }

    /**
     * 用户详情（含角色 ID 列表，用于编辑回显）
     */
    public UserVO detail(Integer userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toVO(user);
    }

    /**
     * 新增用户：login_name 唯一校验 → 默认密码=MD5(login_name) → 角色绑定
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer create(UserSaveRequest req) {
        checkLoginNameUnique(req.getLoginName(), null);
        SysUser user = new SysUser();
        user.setUserName(req.getUserName());
        user.setLoginName(req.getLoginName());
        // 密码：不传则默认登录名 MD5（与老系统 save 一致）；传了则 MD5(明文)
        String pwd = StringUtils.hasText(req.getPassword()) ? req.getPassword() : req.getLoginName();
        user.setPassword(md5(pwd));
        user.setTelephone(req.getTelephone());
        user.setEmail(req.getEmail());
        user.setQualityAgCode(req.getQualityAgCode());
        user.setQualityAgDN(req.getQualityAgDN());
        user.setSkillist(req.getSkillist());
        user.setTech(req.getTech());
        user.setRoleType(req.getRoleType() == null ? null : req.getRoleType().byteValue());
        sysUserMapper.insert(user);
        bindRoles(user.getUserId(), req.getRoleIds());
        return user.getUserId();
    }

    /**
     * 更新用户（密码留空不改）+ 角色覆盖式绑定
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Integer userId, UserSaveRequest req) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        checkLoginNameUnique(req.getLoginName(), userId);
        user.setUserName(req.getUserName());
        user.setLoginName(req.getLoginName());
        if (StringUtils.hasText(req.getPassword())) {
            user.setPassword(md5(req.getPassword()));
        }
        user.setTelephone(req.getTelephone());
        user.setEmail(req.getEmail());
        user.setQualityAgCode(req.getQualityAgCode());
        user.setQualityAgDN(req.getQualityAgDN());
        user.setSkillist(req.getSkillist());
        user.setTech(req.getTech());
        user.setRoleType(req.getRoleType() == null ? null : req.getRoleType().byteValue());
        sysUserMapper.updateById(user);
        bindRoles(userId, req.getRoleIds());
    }

    /**
     * 删除用户：若已签入（RT_Agent 有对应坐席）则拒绝
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(user.getQualityAgCode()) && checkAgentExist(user.getQualityAgCode())) {
            throw new BusinessException("该用户正在签入，无法删除");
        }
        sysUserroleMapper.delete(new QueryWrapper<SysUserrole>().eq("user_id", userId));
        sysUserMapper.deleteById(userId);
    }

    /**
     * 重置密码为登录名 MD5（与老系统 resetPwd 一致）
     */
    public void resetPassword(Integer userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(md5(user.getLoginName()));
        sysUserMapper.updateById(user);
    }

    /**
     * 当前用户修改自己密码（校验原密码）
     */
    public void changePassword(Integer userId, ChangePasswordRequest req) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!md5(req.getOldPassword()).equalsIgnoreCase(user.getPassword())) {
            throw new BusinessException("原始密码错误");
        }
        user.setPassword(md5(req.getNewPassword()));
        sysUserMapper.updateById(user);
    }

    /**
     * 登录名唯一性校验（老系统 checkLoginName）
     * @param excludeUserId 编辑时排除自身
     */
    public boolean isLoginNameAvailable(String loginName, Integer excludeUserId) {
        QueryWrapper<SysUser> qw = new QueryWrapper<SysUser>().eq("login_name", loginName);
        if (excludeUserId != null) {
            qw.ne("user_id", excludeUserId);
        }
        return sysUserMapper.selectCount(qw) == 0;
    }

    /**
     * 坐席工号唯一性校验（老系统 checkAgentId）
     */
    public boolean isAgentCodeAvailable(String qualityAgCode, Integer excludeUserId) {
        if (!StringUtils.hasText(qualityAgCode)) {
            return false;
        }
        QueryWrapper<SysUser> qw = new QueryWrapper<SysUser>().eq("qualityAgCode", qualityAgCode);
        if (excludeUserId != null) {
            qw.ne("user_id", excludeUserId);
        }
        return sysUserMapper.selectCount(qw) == 0;
    }

    /** 查询用户坐席工号（跨 Service 调用，@DS 注解经代理生效） */
    public String agentCodeOf(Integer userId) {
        if (userId == null) {
            return null;
        }
        SysUser user = sysUserMapper.selectById(userId);
        return user != null ? user.getQualityAgCode() : null;
    }

    private void checkLoginNameUnique(String loginName, Integer excludeUserId) {
        if (!isLoginNameAvailable(loginName, excludeUserId)) {
            throw new BusinessException("登录名 " + loginName + " 已存在");
        }
    }

    /**
     * 角色覆盖式绑定：先删后插（与老系统 Db.tx 逻辑一致）
     */
    private void bindRoles(Integer userId, List<Integer> roleIds) {
        sysUserroleMapper.delete(new QueryWrapper<SysUserrole>().eq("user_id", userId));
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Integer roleId : roleIds.stream().distinct().collect(Collectors.toList())) {
                if (roleId == null) {
                    continue;
                }
                SysUserrole ur = new SysUserrole();
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                sysUserroleMapper.insert(ur);
            }
        }
    }

    /**
     * 检查坐席是否在 RT_Agent 中签入（老系统 checkAgentExist 调 CTI 服务）
     * 服务不可达时视为未签入（不阻塞删除），记日志
     */
    private boolean checkAgentExist(String agentId) {
        try {
            String sql = "SELECT Distinct t1.AgentID FROM RT_Agent t1 where t1.AgentID = " + agentId;
            java.util.LinkedHashMap<String, String> sqlMap = new java.util.LinkedHashMap<>();
            sqlMap.put("sql", sql);
            byte[] result = com.smt.refactor.utils.HttpUtils.httpFormRequest(
                    ctiMonitorServer + "/sqlQuery", sqlMap, false, -1, 1000, 2000);
            if (result == null || new String(result, StandardCharsets.UTF_8).indexOf("[]") > 0) {
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("CTI 服务不可达，视为未签入: {}", e.getMessage());
            return false;
        }
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        vo.setUserId(user.getUserId());
        vo.setUserName(user.getUserName());
        vo.setLoginName(user.getLoginName());
        vo.setTelephone(user.getTelephone());
        vo.setEmail(user.getEmail());
        vo.setQualityAgCode(user.getQualityAgCode());
        vo.setQualityAgDN(user.getQualityAgDN());
        vo.setSkillist(user.getSkillist());
        vo.setTech(user.getTech());
        vo.setRoleType(user.getRoleType() != null ? user.getRoleType().intValue() : null);
        // 角色名列表
        List<Integer> roleIds = sysUserroleMapper.selectList(
                        new QueryWrapper<SysUserrole>().eq("user_id", user.getUserId()))
                .stream().map(SysUserrole::getRoleId).collect(Collectors.toList());
        List<String> roles = new ArrayList<>();
        if (!roleIds.isEmpty()) {
            roles = sysRoleMapper.selectBatchIds(roleIds).stream()
                    .map(SysRole::getRoleName).collect(Collectors.toList());
        }
        vo.setRoles(roles);
        return vo;
    }

    private String md5(String text) {
        return DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
    }
}
