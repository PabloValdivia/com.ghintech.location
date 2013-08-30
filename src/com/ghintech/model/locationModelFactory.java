package com.ghintech.model;

import java.sql.ResultSet;

import org.adempiere.base.IModelFactory;
import org.compiere.model.PO;
import org.compiere.util.Env;

public class locationModelFactory implements IModelFactory {

	@Override
	public Class<?> getClass(String tableName) {
		// TODO Auto-generated method stub
		if(tableName.equals(MMunicipality.Table_Name))
			return MMunicipality.class;
		if(tableName.equals(MParish.Table_Name))
			return MParish.class;
		return null;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {
		// TODO Auto-generated method stub
		if(tableName.equals(MMunicipality.Table_Name))
			return new MMunicipality(Env.getCtx(),Record_ID,trxName);
		if(tableName.equals(MParish.Table_Name))
			return new MParish(Env.getCtx(),Record_ID,trxName);
		return null;
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {
		// TODO Auto-generated method stub
		if(tableName.equals(MMunicipality.Table_Name))
			return new MMunicipality(Env.getCtx(),rs,trxName);
		if(tableName.equals(MParish.Table_Name))
			return new MParish(Env.getCtx(),rs,trxName);
		return null;
	}

}
