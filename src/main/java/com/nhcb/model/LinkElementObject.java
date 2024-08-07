package com.nhcb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LinkElementObject {
    private String columnName;
    private String element;
    private String elementFind;
    private String elementName;
    private String file;
    private String openWindowDownload;
    private String id;
    private Boolean nullableOrPass;
    private Object nullableOrElseGet;
}
