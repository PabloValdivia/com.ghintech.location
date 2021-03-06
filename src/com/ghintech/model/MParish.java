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

/**
 *	Localtion Parish Model (Value Object)
 *
 *  @author 	Jorg Janke
 *  @version 	$Id: MParish.java,v 1.3 2006/07/30 00:58:36 jjanke Exp $
 */
public final class MParish extends X_C_Parish
	implements DocAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1124865777747582617L;
	private static final int COUNTRY_JAPAN = 0;


	/**
	 * 	Load Parishs (cached)
	 *	@param ctx context
	 */
	private static void loadAllParishs (Properties ctx)
	{
		s_Parishs = new CCache<String,MParish>("C_Parish", 100);
		String sql = "SELECT * FROM C_Parish WHERE IsActive='Y'";
		try
		{
			Statement stmt = DB.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while(rs.next())
			{
				MParish r = new MParish (ctx, rs, null);
				s_Parishs.put(String.valueOf(r.getC_Parish_ID()), r);
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
		s_log.fine(s_Parishs.size() + " - default=" + s_default);
	}	//	loadAllParishs

	/**
	 * 	Get Country (cached)
	 * 	@param ctx context
	 *	@param C_Parish_ID ID
	 *	@return Country
	 */
	public static MParish get (Properties ctx, int C_Parish_ID)
	{
		if (s_Parishs == null || s_Parishs.size() == 0)
			loadAllParishs(ctx);
		String key = String.valueOf(C_Parish_ID);
		MParish r = (MParish)s_Parishs.get(key);
		if (r != null)
			return r;
		r = new MParish (ctx, C_Parish_ID, null);
		if (r.getC_Parish_ID() == C_Parish_ID)
		{
			s_Parishs.put(key, r);
			return r;
		}
		return null;
	}	//	get

	/**
	 * 	Get Default Parish
	 * 	@param ctx context
	 *	@return Parish or null
	 */
	public static MParish getDefault (Properties ctx)
	{
		if (s_Parishs == null || s_Parishs.size() == 0)
			loadAllParishs(ctx);
		return s_default;
	}	//	get

	/**
	 *	Return Parishs as Array
	 * 	@param ctx context
	 *  @return MCountry Array
	 */
	@SuppressWarnings("unchecked")
	public static MParish[] getParishs(Properties ctx)
	{
		if (s_Parishs == null || s_Parishs.size() == 0)
			loadAllParishs(ctx);
		MParish[] retValue = new MParish[s_Parishs.size()];
		s_Parishs.values().toArray(retValue);
		Arrays.sort(retValue, new MParish(ctx, 0, null));
		return retValue;
	}	//	getParishs

	/**
	 *	Return Array of Parishs of Country
	 * 	@param ctx context
	 *  @param C_Country_ID country
	 *  @return MParish Array
	 */
	@SuppressWarnings("unchecked")
	public static MParish[] getParishs (Properties ctx, int C_Municipality_ID, int C_Region_ID)
	{
		if (s_Parishs == null || s_Parishs.size() == 0)
			loadAllParishs(ctx);
		ArrayList<MParish> list = new ArrayList<MParish>();
		Iterator it = s_Parishs.values().iterator();
		while (it.hasNext())
		{
			MParish r = (MParish)it.next();
			if (r.getC_Municipality_ID() == C_Municipality_ID && r.getC_Region_ID()==C_Region_ID)
				list.add(r);
		}
		//  Sort it
		MParish[] retValue = new MParish[list.size()];
		list.toArray(retValue);
		Arrays.sort(retValue, new MParish(ctx, 0, null));
		return retValue;
	}	//	getParishs

	/**	Parish Cache				*/
	private static CCache<String,MParish> s_Parishs = null;
	/** Default Parish				*/
	private static MParish		s_default = null;
	/**	Static Logger				*/
	private static CLogger		s_log = CLogger.getCLogger (MParish.class);

	
	/**************************************************************************
	 *	Create empty Parish
	 * 	@param ctx context
	 * 	@param C_Parish_ID id
	 *	@param trxName transaction
	 */
	public MParish (Properties ctx, int C_Parish_ID, String trxName)
	{
		super (ctx, C_Parish_ID, trxName);
		if (C_Parish_ID == 0)
		{
		}
	}   //  MParish

	/**
	 *	Create Parish from current row in ResultSet
	 * 	@param ctx context
	 *  @param rs result set
	 *	@param trxName transaction
	 */
	public MParish (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MParish

	/**
	 * 	Parent Constructor
	 *	@param country country
	 *	@param ParishName Parish Name
	 */
	public MParish (MCountry country, String ParishName)
	{
		super (country.getCtx(), 0, country.get_TrxName());
		setC_Country_ID(country.getC_Country_ID());
		setName(ParishName);
	}   //  MParish
	
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
		/** To add your Parishs, complete the code below.
		 * 	Please make sure that the file is converted via the Java utility
		 * 	native2ascii - i.e. all seven bit code with /u0000 unicode stuff 
		 */
		int C_Country_ID = COUNTRY_JAPAN;		//	Japan
		MCountry country = new MCountry(Env.getCtx(), C_Country_ID, null); 
		// Hokkaido
		MParish temp = new MParish (country, "\u5317\u6d77\u9053");
		temp.setDescription( "\u5317\u6d77\u9053(Hokkaido)" );
		temp.saveEx();
		// Aomori
		temp = new MParish (country, "\u9752\u68ee\u770c");
		temp.setDescription( "\u9752\u68ee\u770c(Aomori)" );
		temp.saveEx();
		// Iwate
		temp = new MParish (country, "\u5ca9\u624b\u770c");
		temp.setDescription( "\u5ca9\u624b\u770c(Iwate)" );
		temp.saveEx();
		// Miyagi
		temp = new MParish (country, "\u5bae\u57ce\u770c");
		temp.setDescription( "\u5bae\u57ce\u770c(Miyagi)" );
		temp.saveEx();
		// Akita
		temp = new MParish (country, "\u79cb\u7530\u770c");
		temp.setDescription( "\u79cb\u7530\u770c(Akita)" );
		temp.saveEx();
		// Yamagata
		temp = new MParish (country, "\u5c71\u5f62\u770c");
		temp.setDescription( "\u5c71\u5f62\u770c(Yamagata)" );
		temp.saveEx();
		// Fukushima
		temp = new MParish (country, "\u798f\u5cf6\u770c");
		temp.setDescription( "\u798f\u5cf6\u770c(Fukushima)" );
		temp.saveEx();
		// Ibaraki
		temp = new MParish (country, "\u8328\u57ce\u770c");
		temp.setDescription( "\u8328\u57ce\u770c(Ibaraki)" );
		temp.saveEx();
		// Gunma
		temp = new MParish (country, "\u7fa4\u99ac\u770c");
		temp.setDescription( "\u7fa4\u99ac\u770c(Gunma)" );
		temp.saveEx();
		// Saitama
		temp = new MParish (country, "\u57fc\u7389\u770c");
		temp.setDescription( "\u57fc\u7389\u770c(Saitama)" );
		temp.saveEx();
		// Chiba
		temp = new MParish (country, "\u5343\u8449\u770c");
		temp.setDescription( "\u5343\u8449\u770c(Chiba)" );
		temp.saveEx();
		// Tokyo
		temp = new MParish (country, "\u6771\u4eac\u90fd");
		temp.setDescription( "\u6771\u4eac\u90fd(Tokyo)" );
		temp.saveEx();
		// Kanagawa
		temp = new MParish (country, "\u795e\u5948\u5ddd\u770c");
		temp.setDescription( "\u795e\u5948\u5ddd\u770c(Kanagawa)" );
		temp.saveEx();
		// Niigata
		temp = new MParish (country, "\u65b0\u6f5f\u770c");
		temp.setDescription( "\u65b0\u6f5f\u770c(Niigata)" );
		temp.saveEx();
		// Toyama
		temp = new MParish (country, "\u5bcc\u5c71\u770c");
		temp.setDescription( "\u5bcc\u5c71\u770c(Toyama)" );
		temp.saveEx();
		// Ishikawa
		temp = new MParish (country, "\u77f3\u5ddd\u770c");
		temp.setDescription( "\u77f3\u5ddd\u770c(Ishikawa)" );
		temp.saveEx();
		// Fukui
		temp = new MParish (country, "\u798f\u4e95\u770c");
		temp.setDescription( "\u798f\u4e95\u770c(Fukui)" );
		temp.saveEx();
		// Yamanashi
		temp = new MParish (country, "\u5c71\u68a8\u770c");
		temp.setDescription( "\u5c71\u68a8\u770c(Yamanashi)" );
		temp.saveEx();
		// Gifu
		temp = new MParish (country, "\u5c90\u961c\u770c");
		temp.setDescription( "\u5c90\u961c\u770c(Gifu)" );
		temp.saveEx();
		// Shizuoka
		temp = new MParish (country, "\u9759\u5ca1\u770c");
		temp.setDescription( "\u9759\u5ca1\u770c(Shizuoka)" );
		temp.saveEx();
		// Aichi
		temp = new MParish (country, "\u611b\u77e5\u770c");
		temp.setDescription( "\u611b\u77e5\u770c(Aichi)" );
		temp.saveEx();
		// Mie
		temp = new MParish (country, "\u4e09\u91cd\u770c");
		temp.setDescription( "\u4e09\u91cd\u770c(Mie)" );
		temp.saveEx();
		// Siga
		temp = new MParish (country, "\u6ecb\u8cc0\u770c");
		temp.setDescription( "\u6ecb\u8cc0\u770c(Siga)" );
		temp.saveEx();
		// Kyoto
		temp = new MParish (country, "\u4eac\u90fd\u5e9c");
		temp.setDescription( "\u4eac\u90fd\u5e9c(Kyoto)" );
		temp.saveEx();
		// Osaka
		temp = new MParish (country, "\u5927\u962a\u5e9c");
		temp.setDescription( "\u5927\u962a\u5e9c(Osaka)" );
		temp.saveEx();
		// Hyogo
		temp = new MParish (country, "\u5175\u5eab\u770c");
		temp.setDescription( "\u5175\u5eab\u770c(Hyogo)" );
		temp.saveEx();
		// Nara
		temp = new MParish (country, "\u5948\u826f\u770c");
		temp.setDescription( "\u5948\u826f\u770c(Nara)" );
		temp.saveEx();
		// Wakayama
		temp = new MParish (country, "\u548c\u6b4c\u5c71\u770c");
		temp.setDescription( "\u548c\u6b4c\u5c71\u770c(Wakayama)" );
		temp.saveEx();
		// Tottori
		temp = new MParish (country, "\u9ce5\u53d6\u770c");
		temp.setDescription( "\u9ce5\u53d6\u770c(Tottori)" );
		temp.saveEx();
		// Shimane
		temp = new MParish (country, "\u5cf6\u6839\u770c");
		temp.setDescription( "\u5cf6\u6839\u770c(Shimane)" );
		temp.saveEx();
		// Okayama
		temp = new MParish (country, "\u5ca1\u5c71\u770c");
		temp.setDescription( "\u5ca1\u5c71\u770c(Okayama)" );
		temp.saveEx();
		// Hiroshima
		temp = new MParish (country, "\u5e83\u5cf6\u770c");
		temp.setDescription( "\u5e83\u5cf6\u770c(Hiroshima)" );
		temp.saveEx();
		// Yamaguchi
		temp = new MParish (country, "\u5c71\u53e3\u770c");
		temp.setDescription( "\u5c71\u53e3\u770c(Yamaguchi)" );
		temp.saveEx();
		// Tokushima
		temp = new MParish (country, "\u5fb3\u5cf6\u770c");
		temp.setDescription( "\u5fb3\u5cf6\u770c(Tokushima)" );
		temp.saveEx();
		// Kagawa
		temp = new MParish (country, "\u9999\u5ddd\u770c");
		temp.setDescription( "\u9999\u5ddd\u770c(Kagawa)" );
		temp.saveEx();
		// Ehime
		temp = new MParish (country, "\u611b\u5a9b\u770c");
		temp.setDescription( "\u611b\u5a9b\u770c(Ehime)" );
		temp.saveEx();
		// Kouchi
		temp = new MParish (country, "\u9ad8\u77e5\u770c");
		temp.setDescription( "\u9ad8\u77e5\u770c(Kouchi)" );
		temp.saveEx();
		// Fukuoka
		temp = new MParish (country, "\u798f\u5ca1\u770c");
		temp.setDescription( "\u798f\u5ca1\u770c(Fukuoka)" );
		temp.saveEx();
		// Saga
		temp = new MParish (country, "\u4f50\u8cc0\u770c");
		temp.setDescription( "\u4f50\u8cc0\u770c(Saga)" );
		temp.saveEx();
		// Nagasaki
		temp = new MParish (country, "\u9577\u5d0e\u770c");
		temp.setDescription( "\u9577\u5d0e\u770c(Nagasaki)" );
		temp.saveEx();
		// Kumamoto
		temp = new MParish (country, "\u718a\u672c\u770c");
		temp.setDescription( "\u718a\u672c\u770c(Kumamoto)" );
		temp.saveEx();
		// Ohita
		temp = new MParish (country, "\u5927\u5206\u770c");
		temp.setDescription( "\u5927\u5206\u770c(Ohita)" );
		temp.saveEx();
		// Miyasaki
		temp = new MParish (country, "\u5bae\u5d0e\u770c");
		temp.setDescription( "\u5bae\u5d0e\u770c(Miyasaki)" );
		temp.saveEx();
		// Kagoshima
		temp = new MParish (country, "\u9e7f\u5150\u5cf6\u770c");
		temp.setDescription( "\u9e7f\u5150\u5cf6\u770c(Kagoshima)" );
		temp.saveEx();
		// Okinawa
		temp = new MParish (country, "\u6c96\u7e04\u770c");
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
	
}	//	MParish
