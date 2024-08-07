package com.nhcb.model;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommandModel {

    private String targetURI;
    private ArrayList<String> targetURIList;
    private ArrayList<Object> targetList;
    private ArrayList<Object> targetRangeList;
    private String tableName;
    private ArrayList<String> columnList;
    private ArrayList<LinkElementObject> linkElementObjectList;
    private String fileDownloadFilePath;
    private String targetLoopNumber;
    private ArrayList<TargetParamsSetting> targetParamsSettingList;

}
