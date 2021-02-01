package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.Tran;

import java.util.List;
import java.util.Map;

public interface TranDao {

    int save(Tran t);

    Tran getTranById(String id);

    int changeStage(Tran t);



    int getTranTotal();

    List<Map<String, Object>> getTranList();
}
