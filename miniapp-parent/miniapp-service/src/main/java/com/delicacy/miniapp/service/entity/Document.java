package com.delicacy.miniapp.service.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by summer on 2017/5/5.
 */
@Data
public class Document implements Serializable {
        private static final long serialVersionUID = -1841675356984907309L;
        private Long id;
        private String title;
        private String dateTime;
        private String content;

}