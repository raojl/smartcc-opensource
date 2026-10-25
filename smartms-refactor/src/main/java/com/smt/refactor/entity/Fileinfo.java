package com.smt.refactor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("cc_core_fileinfo")
public class Fileinfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "file_id", type = IdType.AUTO)
    private Integer fileId;

    @TableField("file_path")
    private String filePath;

    @TableField("file_name")
    private String fileName;

    @TableField("file_type")
    private String fileType;

    @TableField("file_desc")
    private String fileDesc;

    @TableField("create_date")
    private String createDate;

    @TableField("modify_date")
    private String modifyDate;
}
