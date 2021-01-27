package com.bjpowernode.crm.workbench.service.impl;

import com.bjpowernode.crm.utils.DateTimeUtil;
import com.bjpowernode.crm.utils.SqlSessionUtil;
import com.bjpowernode.crm.utils.UUIDUtil;
import com.bjpowernode.crm.workbench.dao.*;
import com.bjpowernode.crm.workbench.domain.*;
import com.bjpowernode.crm.workbench.service.ClueService;

import java.util.List;

public class ClueServiceImpl implements ClueService {
    //线索
    private ClueDao clueDao = SqlSessionUtil.getSqlSession().getMapper(ClueDao.class);
    private ClueActivityRelationDao clueActivityRelationDao = SqlSessionUtil.getSqlSession().getMapper(ClueActivityRelationDao.class);
    private ClueRemarkDao clueRemarkDao = SqlSessionUtil.getSqlSession().getMapper(ClueRemarkDao.class);
    //顾客
    private CustomerDao customerDao = SqlSessionUtil.getSqlSession().getMapper(CustomerDao.class);
    private CustomerRemarkDao customerRemarkDao = SqlSessionUtil.getSqlSession().getMapper(CustomerRemarkDao.class);
    //交易
    private TranDao tranDao = SqlSessionUtil.getSqlSession().getMapper(TranDao.class);
    private TranHistoryDao tranHistoryDao = SqlSessionUtil.getSqlSession().getMapper(TranHistoryDao.class);
    //联系人
    private ContactsDao contactsDao = SqlSessionUtil.getSqlSession().getMapper(ContactsDao.class);
    private ContactsActivityRelationDao contactsActivityRelationDao = SqlSessionUtil.getSqlSession().getMapper(ContactsActivityRelationDao.class);
    private ContactsRemarkDao contactsRemarkDao = SqlSessionUtil.getSqlSession().getMapper(ContactsRemarkDao.class);


    @Override
    public boolean save(Clue clue) {
        ClueDao clueDao = SqlSessionUtil.getSqlSession().getMapper(ClueDao.class);
        boolean flag = true;
        int count = clueDao.save(clue);
        if (count!=1){
            flag=false;
        }
        return flag;
    }

    @Override
    public Clue detail(String id) {
        Clue c = clueDao.detail(id);
        return c;
    }

    @Override
    public boolean unbund(String id) {
        boolean flag = true;
        int count = clueActivityRelationDao.unbund(id);
        if (count!=1){
            flag=false;
        }
        return flag;
    }

    @Override
    public boolean bund(String cid, String[] aids) {
        boolean flag = true;
        for (String aid:aids){
            ClueActivityRelation car = new ClueActivityRelation();
            car.setId(UUIDUtil.getUUID());
            car.setClueId(cid);
            car.setActivityId(aid);
            int count = clueActivityRelationDao.bund(car);
            if (count != 1){
                flag=false;
            }
        }
        return flag;
    }

    @Override
    public boolean convert(String clueId, Tran t, String createBy) {

        String createTime = DateTimeUtil.getSysTime();

        boolean flag = true;
        //(1) 获取到线索id，通过线索id获取线索对象（线索对象当中封装了线索的信息）
        Clue c = clueDao.getById(clueId);
        //取线索中的公司名
        String company = c.getCompany();
        //(2) 通过线索对象提取客户信息，当该客户不存在的时候，新建客户（根据公司的名称精确匹配，判断该客户是否存在！）
        Customer cus = customerDao.getCustomerByName(company);
        if (cus==null){
            cus = new Customer();
            cus.setId(UUIDUtil.getUUID());
            cus.setOwner(c.getOwner());
            cus.setName(company);
            cus.setWebsite(c.getWebsite());
            cus.setPhone(c.getPhone());
            cus.setCreateBy(createBy);
            cus.setCreateTime(createTime);
            cus.setContactSummary(c.getContactSummary());
            cus.setNextContactTime(c.getNextContactTime());
            cus.setDescription(c.getDescription());
            cus.setAddress(c.getAddress());

            //添加客户
            int count1 = customerDao.save(cus);
            if (count1!=1){
                flag=false;
            }
        }
        //(3) 通过线索对象提取联系人信息，保存联系人
        Contacts con = new Contacts();
        con.setId(UUIDUtil.getUUID());
        con.setOwner(c.getOwner());
        con.setSource(c.getSource());
        con.setCustomerId(cus.getId());
        con.setFullname(c.getFullname());
        con.setAppellation(c.getAppellation());
        con.setEmail(c.getEmail());
        con.setMphone(c.getMphone());
        con.setJob(c.getJob());
        con.setCreateBy(createBy);
        con.setCreateTime(createTime);
        con.setDescription(c.getDescription());
        con.setContactSummary(c.getContactSummary());
        con.setAddress(c.getAddress());
        con.setNextContactTime(c.getNextContactTime());
        //添加联系人
        int count2 = contactsDao.save(con);
        if (count2!=1){
            flag=false;
        }
        //(4) 线索备注转换到客户备注以及联系人备注
        //查询出与该线索相关联的备注信息列表
        List<ClueRemark> clueRemarkList = clueRemarkDao.getListByClueId(clueId);
        //取出每一条线索的备注
        for (ClueRemark clueRemark:clueRemarkList){
            //取出备注信息(主要转换到客户备注和联系人备注的就是这个备注信息)
            String noteContext = clueRemark.getNoteContent();
            //创建客户备注对象，添加客户备注
            CustomerRemark customerRemark = new CustomerRemark();
            customerRemark.setId(UUIDUtil.getUUID());
            customerRemark.setNoteContent(noteContext);
            customerRemark.setCreateBy(createBy);
            customerRemark.setCreateTime(createTime);
            customerRemark.setEditFlag("0");
            customerRemark.setCustomerId(cus.getId());
            int count3 = customerRemarkDao.save(customerRemark);
            if (count3!=1){
                flag=false;
            }
            //创建联系人备注对象，添加联系人
            ContactsRemark contactsRemark = new ContactsRemark();
            contactsRemark.setId(UUIDUtil.getUUID());
            contactsRemark.setNoteContent(noteContext);
            contactsRemark.setCreateBy(createBy);
            contactsRemark.setCreateTime(createTime);
            contactsRemark.setEditFlag("0");
            contactsRemark.setContactsId(con.getId());
            int count4 = contactsRemarkDao.save(contactsRemark);
            if (count4!=1){
                flag=false;
            }
        }
        //(5) “线索和市场活动”的关系转换到“联系人和市场活动”的关系
        //查询出与该条线索关联的市场活动，查询与市场活动的关联关系列表
        List<ClueActivityRelation> clueActivityRelationList = clueActivityRelationDao.getListByClueId(clueId);
        //遍历出每一条与市场活动关联的关联关系记录
        for (ClueActivityRelation clueActivityRelation:clueActivityRelationList){
            //从每一条遍历出来的记录中取出关联的市场活动id
            String activityId = clueActivityRelation.getActivityId();
            //创建 联系人与市场活动的关联关系对象 让第三步生成的联系人与市场活动做关联
            ContactsActivityRelation contactsActivityRelation = new ContactsActivityRelation();
            contactsActivityRelation.setId(UUIDUtil.getUUID());
            contactsActivityRelation.setActivityId(activityId);
            contactsActivityRelation.setContactsId(con.getId());

            int count5 = contactsActivityRelationDao.save(contactsActivityRelation);
            if (count5!=1){
                flag=false;
            }
        }
        //(6) 如果有创建交易需求，创建一条交易
        if (t!=null){
            /*

                t对象里已经封装了一些信息
                接下来通过第一步生成的c对象，取出一些信息，完善对t的封装

             */
            t.setSource(c.getSource());
            t.setOwner(c.getOwner());
            t.setNextContactTime(c.getNextContactTime());
            t.setDescription(c.getDescription());
            t.setCustomerId(cus.getId());
            t.setContactSummary(c.getContactSummary());
            t.setContactsId(con.getId());
            //添加交易
            int count6 = tranDao.save(t);
            if (count6!=1){
                flag=false;
            }
            //(7) 如果创建了交易，则创建一条该交易下的交易历史
            TranHistory th = new TranHistory();
            th.setId(UUIDUtil.getUUID());
            th.setStage(t.getStage());
            th.setMoney(t.getMoney());
            th.setExpectedDate(t.getExpectedDate());
            th.setCreateTime(createTime);
            th.setCreateBy(createBy);
            th.setTranId(t.getId());
            //添加历史
            int count7 = tranHistoryDao.save(th);
            if (count7!=1){
                flag=false;
            }
        }
        //(8) 删除线索备注
        for (ClueRemark clueRemark:clueRemarkList){
            int count8 = clueRemarkDao.delete(clueRemark);
            if (count8!=1){
                flag=false;
            }
        }
        //(9) 删除线索和市场活动的关系
        for (ClueActivityRelation clueActivityRelation:clueActivityRelationList){
            int count9 = clueActivityRelationDao.delete(clueActivityRelation);
            if (count9!=1){
                flag=false;
            }
        }
        //(10) 删除线索
        int count10 = clueDao.delete(clueId);
        if (count10!=1){
            flag=false;
        }







        return flag;
    }


}
