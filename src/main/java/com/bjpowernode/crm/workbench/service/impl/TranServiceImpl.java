package com.bjpowernode.crm.workbench.service.impl;

import com.bjpowernode.crm.utils.DateTimeUtil;
import com.bjpowernode.crm.utils.SqlSessionUtil;
import com.bjpowernode.crm.utils.UUIDUtil;
import com.bjpowernode.crm.workbench.dao.CustomerDao;
import com.bjpowernode.crm.workbench.dao.TranDao;
import com.bjpowernode.crm.workbench.dao.TranHistoryDao;
import com.bjpowernode.crm.workbench.domain.Customer;
import com.bjpowernode.crm.workbench.domain.Tran;
import com.bjpowernode.crm.workbench.domain.TranHistory;
import com.bjpowernode.crm.workbench.service.TranService;

import java.util.List;

public class TranServiceImpl implements TranService {
    private TranDao tranDao = SqlSessionUtil.getSqlSession().getMapper(TranDao.class);
    private TranHistoryDao tranHistoryDao = SqlSessionUtil.getSqlSession().getMapper(TranHistoryDao.class);
    private CustomerDao customerDao = SqlSessionUtil.getSqlSession().getMapper(CustomerDao.class);



    @Override
    public boolean save(Tran t, String customerName) {
        //目前没有customerId ,需要先判断是否存在该客户，自动选择创建或者获取该客户
        boolean flag = true;
        Customer cus = customerDao.getCustomerByName(customerName);
        if (cus==null){
            cus = new Customer();
            cus.setId(UUIDUtil.getUUID());
            cus.setName(customerName);
            cus.setCreateBy(t.getCreateBy());
            cus.setCreateTime(t.getCreateTime());
            cus.setContactSummary(t.getContactSummary());
            cus.setNextContactTime(t.getNextContactTime());
            cus.setOwner(t.getOwner());
            //添加客户
            int count1 = customerDao.save(cus);
            if (count1!=1){
                flag=false;
            }
        }
        //客户处理完了
        //将客户的id添加到t
        t.setCustomerId(cus.getId());
        //添加交易
        int count2 = tranDao.save(t);
        if (count2!=1){
            flag=false;
        }
        //添加交易历史
        TranHistory th = new TranHistory();
        th.setId(UUIDUtil.getUUID());
        th.setCreateTime(DateTimeUtil.getSysTime());
        th.setCreateBy(t.getCreateBy());
        th.setTranId(t.getId());
        th.setExpectedDate(t.getExpectedDate());
        th.setMoney(t.getMoney());
        th.setStage(t.getStage());

        int count3 = tranHistoryDao.save(th);
        if (count3!=1){
            flag=false;
        }
        return flag;
    }
}
