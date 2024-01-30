package com.footlocer.mon.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author jht
 * @since 2024-01-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("touch_sku")
public class TouchSku implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sku;

    @TableField("update_time")
    private LocalDateTime updateTime;


}
