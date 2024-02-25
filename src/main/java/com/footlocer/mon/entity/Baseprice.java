package com.footlocer.mon.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author jht
 * @since 2024-02-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="Baseprice对象", description="")
public class Baseprice implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sku;

    private String size;

    private Integer stockxLow;

    private Integer goatLow;


}
