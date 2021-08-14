package com.delicacy.miniapp.service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ControlParam {
    private String key;
    private Integer weigth;
    private Boolean direct;
}
