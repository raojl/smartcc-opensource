package com.smt.refactor.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("tbl_customer_service")
public class CustomerService implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("name")
    private String name;

    @TableField("virt_num")
    private String virtNum;

    @TableField("telephone")
    private String telephone;

    @TableField("user_id")
    private String userId;
}
