package com.smt.refactor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author smartms-refactor
 * @since 2026-08-02
 */
@Getter
@Setter
@TableName("crm_customlevel")
public class Customlevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("custom_id")
    private String customId;

    @TableField("custom_level")
    private Short customLevel;
}
