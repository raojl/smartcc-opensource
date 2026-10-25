package com.smt.refactor.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.smt.refactor.entity.SysMenu;
import com.smt.refactor.entity.SysUser;
import com.smt.refactor.entity.Trunk;
import com.smt.refactor.mapper.SysMenuMapper;
import com.smt.refactor.mapper.SysUserMapper;
import com.smt.refactor.mapper.TrunkMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 配置库（smartconf）数据访问服务 —— 阶段 0 验证用
 */
@Service
@RequiredArgsConstructor
@DS("conf")
public class ConfService {

    private final SysUserMapper sysUserMapper;
    private final SysMenuMapper sysMenuMapper;
    private final TrunkMapper trunkMapper;

    public long countUser() {
        return sysUserMapper.selectCount(null);
    }

    public List<SysUser> listUsers() {
        return sysUserMapper.selectList(null);
    }

    public long countMenu() {
        return sysMenuMapper.selectCount(null);
    }

    public List<Trunk> listTrunks() {
        return trunkMapper.selectList(null);
    }
}
