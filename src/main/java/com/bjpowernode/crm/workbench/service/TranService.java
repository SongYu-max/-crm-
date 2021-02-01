package com.bjpowernode.crm.workbench.service;

import com.bjpowernode.crm.workbench.domain.Tran;
import com.bjpowernode.crm.workbench.domain.TranHistory;

import java.util.List;
import java.util.Map;

public interface TranService {
    boolean save(Tran t, String customerName);

    Tran getTranById(String id);

    List<TranHistory> getTranHistoryByTranId(String tranId);

    boolean changeStage(Tran t);


    Map<String, Object> getCharts();
}
