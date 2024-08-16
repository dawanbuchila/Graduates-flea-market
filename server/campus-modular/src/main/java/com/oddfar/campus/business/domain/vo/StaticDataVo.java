package com.oddfar.campus.business.domain.vo;


import lombok.Data;

import java.util.ArrayList;

@Data
public class StaticDataVo {

    private int userNum;

    private int categoryNum;

    private int addContentNum;

    private int onlineNum;

    private ArrayList<String> categoryNameList;

    private ArrayList<Integer> categoryNumList;

}
