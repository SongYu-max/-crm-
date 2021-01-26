package com.bjpowernode.crm.workbench.dao;

import com.bjpowernode.crm.workbench.domain.ClueActivityRelation;

public interface ClueActivityRelationDao {
    int unbund(String id);

    int bund(ClueActivityRelation car);

}
