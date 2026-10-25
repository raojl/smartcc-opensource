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
@TableName("base_contactinfo")
public class Contactinfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("ContactID")
    private String contactID;

    @TableField("OriANI")
    private String oriANI;

    @TableField("OriDNIS")
    private String oriDNIS;

    @TableField("ChannelType")
    private Short channelType;

    @TableField("CallDirection")
    private Short callDirection;

    @TableField("CallType")
    private Short callType;

    @TableField("StartTime")
    private String startTime;

    @TableField("LastContactID")
    private String lastContactID;

    @TableField("OriContactID")
    private String oriContactID;

    @TableField("PlatformID")
    private String platformID;
}
