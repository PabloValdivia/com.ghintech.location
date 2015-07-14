/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package com.ghintech.model;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import org.compiere.model.*;
import org.compiere.Adempiere;
import org.compiere.process.DocAction;
import org.compiere.util.CCache;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trx;

/**
 *	Localtion Municipality Model (Value Object)
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: MMunicipality.java,v 1.3 2006/07/30 00:58:36 jjanke Exp $
 */
public final class MMunicipality extends X_C_Municipality
	implements DocAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1124865777747582617L;
	private static final int COUNTRY_JAPAN = 0;


	/**
	 * 	Load Municipalitys (cached)
	 *	@param ctx context
	 */
	private static void loadAllMunicipalitys (Properties ctx)
	{
		s_Municipalitys = new CCache<String,MMunicipality>("C_Municipality", 100);
		String sql = "SELECT * FROM C_Municipality WHERE IsActive='Y'";
		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next())
			{
				MMunicipality r = new MMunicipality (ctx, rs, null);
				s_Municipalitys.put(String.valueOf(r.getC_Municipality_ID()), r);
				if (r.isDefault())
					s_default = r;
			}
			rs.close();
			stmt.close();
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, sql, e);
		}
		s_log.fine(s_Municipalitys.size() + " - default=" + s_default);
	}	//	loadAllMunicipalitys

	/**
	 * 	Get Country (cached)
	 * 	@param ctx context
	 *	@param C_Municipality_ID ID
	 *	@return Country
	 */
	public static MMunicipality get (Properties ctx, int C_Municipality_ID)
	{
		if (s_Municipalitys == null || s_Municipalitys.size() == 0)
			loadAllMunicipalitys(ctx);
		String key = String.valueOf(C_Municipality_ID);
		MMunicipality r = (MMunicipality)s_Municipalitys.get(key);
		if (r != null)
			return r;
		r = new MMunicipality (ctx, C_Municipality_ID, null);
		if (r.getC_Municipality_ID() == C_Municipality_ID)
		{
			s_Municipalitys.put(key, r);
			return r;
		}
		return null;
	}	//	get

	/**
	 * 	Get Default Municipality
	 * 	@param ctx context
	 *	@return Municipality or null
	 */
	public static MMunicipality getDefault (Properties ctx)
	{
		if (s_Municipalitys == null || s_Municipalitys.size() == 0)
			loadAllMunicipalitys(ctx);
		return s_default;
	}	//	get

	/**
	 *	Return Municipalitys as Array
	 * 	@param ctx context
	 *  @return MCountry Array
	 */
	@SuppressWarnings("unchecked")
	public static MMunicipality[] getMunicipalitys(Properties ctx)
	{
		if (s_Municipalitys == null || s_Municipalitys.size() == 0)
			loadAllMunicipalitys(ctx);
		MMunicipality[] retValue = new MMunicipality[s_Municipalitys.size()];
		s_Municipalitys.values().toArray(retValue);
		Arrays.sort(retValue, new MMunicipality(ctx, 0, null));
		return retValue;
	}	//	getMunicipalitys

	/**
	 *	Return Array of Municipalitys of Country
	 * 	@param ctx context
	 *  @param C_Country_ID country
	 *  @return MMunicipality Array
	 */
	@SuppressWarnings("unchecked")
	public static MMunicipality[] getMunicipalitys (Properties ctx, int C_Region_ID)
	{
		if (s_Municipalitys == null || s_Municipalitys.size() == 0)
			loadAllMunicipalitys(ctx);
		ArrayList<MMunicipality> list = new ArrayList<MMunicipality>();
		Iterator it = s_Municipalitys.values().iterator();
		while (it.hasNext())
		{
			MMunicipality r = (MMunicipality)it.next();
			if (r.getC_Region_ID() == C_Region_ID)
				list.add(r);
		}
		//  Sort it
		MMunicipality[] retValue = new MMunicipality[list.size()];
		list.toArray(retValue);
		Arrays.sort(retValue, new MMunicipality(ctx, 0, null));
		return retValue;
	}	//	getMunicipalitys

	/**	Municipality Cache				*/
	private static CCache<String,MMunicipality> s_Municipalitys = null;
	/** Default Municipality				*/
	private static MMunicipality		s_default = null;
	/**	Static Logger				*/
	private static CLogger		s_log = CLogger.getCLogger (MMunicipality.class);

	
	/**************************************************************************
	 *	Create empty Municipality
	 * 	@param ctx context
	 * 	@param C_Municipality_ID id
	 *	@param trxName transaction
	 */
	public MMunicipality (Properties ctx, int C_Municipality_ID, String trxName)
	{
		super (ctx, C_Municipality_ID, trxName);
		if (C_Municipality_ID == 0)
		{
		}
	}   //  MMunicipality

	/**
	 *	Create Municipality from current row in ResultSet
	 * 	@param ctx context
	 *  @param rs result set
	 *	@param trxName transaction
	 */
	public MMunicipality (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MMunicipality

	/**
	 * 	Parent Constructor
	 *	@param country country
	 *	@param MunicipalityName Municipality Name
	 */
	public MMunicipality (MCountry country, String MunicipalityName)
	{
		super (country.getCtx(), 0, country.get_TrxName());
		setC_Country_ID(country.getC_Country_ID());
		setName(MunicipalityName);
	}   //  MMunicipality
	
	/**
	 *	Return Name
	 *  @return Name
	 */
	public String toString()
	{
		return getName();
	}   //  toString

	/**
	 *  Compare
	 *  @param o1 object 1
	 *  @param o2 object 2
	 *  @return -1,0, 1
	 */
	public int compare(Object o1, Object o2)
	{
		String s1 = o1.toString();
		if (s1 == null)
			s1 = "";
		String s2 = o2.toString();
		if (s2 == null)
			s2 = "";
		return s1.compareTo(s2);
	}	//	compare

	/**
	 * 	Test / Load
	 *	@param args
	 */
	public static void main (String[] args)
	{
		Adempiere.startup(true);
		/** To add your Municipalitys, complete the code below.
		 * 	Please make sure that the file is converted via the Java utility
		 * 	native2ascii - i.e. all seven bit code with /u0000 unicode stuff 
		 */
		int C_Country_ID = COUNTRY_JAPAN;		//	Japan
		MCountry country = new MCountry(Env.getCtx(), C_Country_ID, null); 
		// Hokkaido
		MMunicipality temp = new MMunicipality (country, "\u5317\u6d77\u9053");
		temp.setDescription( "\u5317\u6d77\u9053(Hokkaido)" );
		temp.saveEx();
		// Aomori
		temp = new MMunicipality (country, "\u9752\u68ee\u770c");
		temp.setDescription( "\u9752\u68ee\u770c(Aomori)" );
		temp.saveEx();
		// Iwate
		temp = new MMunicipality (country, "\u5ca9\u624b\u770c");
		temp.setDescription( "\u5ca9\u624b\u770c(Iwate)" );
		temp.saveEx();
		// Miyagi
		temp = new MMunicipality (country, "\u5bae\u57ce\u770c");
		temp.setDescription( "\u5bae\u57ce\u770c(Miyagi)" );
		temp.saveEx();
		// Akita
		temp = new MMunicipality (country, "\u79cb\u7530\u770c");
		temp.setDescription( "\u79cb\u7530\u770c(Akita)" );
		temp.saveEx();
		// Yamagata
		temp = new MMunicipality (country, "\u5c71\u5f62\u770c");
		temp.setDescription( "\u5c71\u5f62\u770c(Yamagata)" );
		temp.saveEx();
		// Fukushima
		temp = new MMunicipality (country, "\u798f\u5cf6\u770c");
		temp.setDescription( "\u798f\u5cf6\u770c(Fukushima)" );
		temp.saveEx();
		// Ibaraki
		temp = new MMunicipality (country, "\u8328\u57ce\u770c");
		temp.setDescription( "\u8328\u57ce\u770c(Ibaraki)" );
		temp.saveEx();
		// Gunma
		temp = new MMunicipality (country, "\u7fa4\u99ac\u770c");
		temp.setDescription( "\u7fa4\u99ac\u770c(Gunma)" );
		temp.saveEx();
		// Saitama
		temp = new MMunicipality (country, "\u57fc\u7389\u770c");
		temp.setDescription( "\u57fc\u7389\u770c(Saitama)" );
		temp.saveEx();
		// Chiba
		temp = new MMunicipality (country, "\u5343\u8449\u770c");
		temp.setDescription( "\u5343\u8449\u770c(Chiba)" );
		temp.saveEx();
		// Tokyo
		temp = new MMunicipality (country, "\u6771\u4eac\u90fd");
		temp.setDescription( "\u6771\u4eac\u90fd(Tokyo)" );
		temp.saveEx();
		// Kanagawa
		temp = new MMunicipality (country, "\u795e\u5948\u5ddd\u770c");
		temp.setDescription( "\u795e\u5948\u5ddd\u770c(Kanagawa)" );
		temp.saveEx();
		// Niigata
		temp = new MMunicipality (country, "\u65b0\u6f5f\u770c");
		temp.setDescription( "\u65b0\u6f5f\u770c(Niigata)" );
		temp.saveEx();
		// Toyama
		temp = new MMunicipality (country, "\u5bcc\u5c71\u770c");
		temp.setDescription( "\u5bcc\u5c71\u770c(Toyama)" );
		temp.saveEx();
		// Ishikawa
		temp = new MMunicipality (country, "\u77f3\u5ddd\u770c");
		temp.setDescription( "\u77f3\u5ddd\u770c(Ishikawa)" );
		temp.saveEx();
		// Fukui
		temp = new MMunicipality (country, "\u798f\u4e95\u770c");
		temp.setDescription( "\u798f\u4e95\u770c(Fukui)" );
		temp.saveEx();
		// Yamanashi
		temp = new MMunicipality (country, "\u5c71\u68a8\u770c");
		temp.setDescription( "\u5c71\u68a8\u770c(Yamanashi)" );
		temp.saveEx();
		// Gifu
		temp = new MMunicipality (country, "\u5c90\u961c\u770c");
		temp.setDescription( "\u5c90\u961c\u770c(Gifu)" );
		temp.saveEx();
		// Shizuoka
		temp = new MMunicipality (country, "\u9759\u5ca1\u770c");
		temp.setDescription( "\u9759\u5ca1\u770c(Shizuoka)" );
		temp.saveEx();
		// Aichi
		temp = new MMunicipality (country, "\u611b\u77e5\u770c");
		temp.setDescription( "\u611b\u77e5\u770c(Aichi)" );
		temp.saveEx();
		// Mie
		temp = new MMunicipality (country, "\u4e09\u91cd\u770c");
		temp.setDescription( "\u4e09\u91cd\u770c(Mie)" );
		temp.saveEx();
		// Siga
		temp = new MMunicipality (country, "\u6ecb\u8cc0\u770c");
		temp.setDescription( "\u6ecb\u8cc0\u770c(Siga)" );
		temp.saveEx();
		// Kyoto
		temp = new MMunicipality (country, "\u4eac\u90fd\u5e9c");
		temp.setDescription( "\u4eac\u90fd\u5e9c(Kyoto)" );
		temp.saveEx();
		// Osaka
		temp = new MMunicipality (country, "\u5927\u962a\u5e9c");
		temp.setDescription( "\u5927\u962a\u5e9c(Osaka)" );
		temp.saveEx();
		// Hyogo
		temp = new MMunicipality (country, "\u5175\u5eab\u770c");
		temp.setDescription( "\u5175\u5eab\u770c(Hyogo)" );
		temp.saveEx();
		// Nara
		temp = new MMunicipality (country, "\u5948\u826f\u770c");
		temp.setDescription( "\u5948\u826f\u770c(Nara)" );
		temp.saveEx();
		// Wakayama
		temp = new MMunicipality (country, "\u548c\u6b4c\u5c71\u770c");
		temp.setDescription( "\u548c\u6b4c\u5c71\u770c(Wakayama)" );
		temp.saveEx();
		// Tottori
		temp = new MMunicipality (country, "\u9ce5\u53d6\u770c");
		temp.setDescription( "\u9ce5\u53d6\u770c(Tottori)" );
		temp.saveEx();
		// Shimane
		temp = new MMunicipality (country, "\u5cf6\u6839\u770c");
		temp.setDescription( "\u5cf6\u6839\u770c(Shimane)" );
		temp.saveEx();
		// Okayama
		temp = new MMunicipality (country, "\u5ca1\u5c71\u770c");
		temp.setDescription( "\u5ca1\u5c71\u770c(Okayama)" );
		temp.saveEx();
		// Hiroshima
		temp = new MMunicipality (country, "\u5e83\u5cf6\u770c");
		temp.setDescription( "\u5e83\u5cf6\u770c(Hiroshima)" );
		temp.saveEx();
		// Yamaguchi
		temp = new MMunicipality (country, "\u5c71\u53e3\u770c");
		temp.setDescription( "\u5c71\u53e3\u770c(Yamaguchi)" );
		temp.saveEx();
		// Tokushima
		temp = new MMunicipality (country, "\u5fb3\u5cf6\u770c");
		temp.setDescription( "\u5fb3\u5cf6\u770c(Tokushima)" );
		temp.saveEx();
		// Kagawa
		temp = new MMunicipality (country, "\u9999\u5ddd\u770c");
		temp.setDescription( "\u9999\u5ddd\u770c(Kagawa)" );
		temp.saveEx();
		// Ehime
		temp = new MMunicipality (country, "\u611b\u5a9b\u770c");
		temp.setDescription( "\u611b\u5a9b\u770c(Ehime)" );
		temp.saveEx();
		// Kouchi
		temp = new MMunicipality (country, "\u9ad8\u77e5\u770c");
		temp.setDescription( "\u9ad8\u77e5\u770c(Kouchi)" );
		temp.saveEx();
		// Fukuoka
		temp = new MMunicipality (country, "\u798f\u5ca1\u770c");
		temp.setDescription( "\u798f\u5ca1\u770c(Fukuoka)" );
		temp.saveEx();
		// Saga
		temp = new MMunicipality (country, "\u4f50\u8cc0\u770c");
		temp.setDescription( "\u4f50\u8cc0\u770c(Saga)" );
		temp.saveEx();
		// Nagasaki
		temp = new MMunicipality (country, "\u9577\u5d0e\u770c");
		temp.setDescription( "\u9577\u5d0e\u770c(Nagasaki)" );
		temp.saveEx();
		// Kumamoto
		temp = new MMunicipality (country, "\u718a\u672c\u770c");
		temp.setDescription( "\u718a\u672c\u770c(Kumamoto)" );
		temp.saveEx();
		// Ohita
		temp = new MMunicipality (country, "\u5927\u5206\u770c");
		temp.setDescription( "\u5927\u5206\u770c(Ohita)" );
		temp.saveEx();
		// Miyasaki
		temp = new MMunicipality (country, "\u5bae\u5d0e\u770c");
		temp.setDescription( "\u5bae\u5d0e\u770c(Miyasaki)" );
		temp.saveEx();
		// Kagoshima
		temp = new MMunicipality (country, "\u9e7f\u5150\u5cf6\u770c");
		temp.setDescription( "\u9e7f\u5150\u5cf6\u770c(Kagoshima)" );
		temp.saveEx();
		// Okinawa
		temp = new MMunicipality (country, "\u6c96\u7e04\u770c");
		temp.setDescription( "\u6c96\u7e04\u770c(Okinawa)" );
		temp.saveEx();

	}	//	main

	@Override
	public void setDocStatus(String newStatus) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getDocStatus() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean processIt(String action) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unlockIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean invalidateIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String prepareIt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean approveIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean rejectIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String completeIt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean voidIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean closeIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reverseCorrectIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reverseAccrualIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean reActivateIt() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentNo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocumentInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File createPDF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProcessMsg() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDoc_User_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getC_Currency_ID() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public BigDecimal getApprovalAmt() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDocAction() {
		// TODO Auto-generated method stub
		return null;
	}
	
}	//	MMunicipality
