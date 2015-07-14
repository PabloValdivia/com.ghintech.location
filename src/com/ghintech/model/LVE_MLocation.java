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
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.process.DocAction;
import org.compiere.util.CCache;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Util;
import org.compiere.model.*;
/**
 *	Location (Address)
 *	
 *  @author Jorg Janke
 *  @version $Id: MLocation.java,v 1.3 2006/07/30 00:54:54 jjanke Exp $
 *  
 *  @author Michael Judd (Akuna Ltd)
 * 				<li>BF [ 2695078 ] Country is not translated on invoice
 * 				<li>FR [2794312 ] Location AutoComplete - check if allow cities out of list
 * 
 * @author Teo Sarca, teo.sarca@gmail.com
 * 		<li>BF [ 3002736 ] MLocation.get cache all MLocations
 * 			https://sourceforge.net/tracker/?func=detail&aid=3002736&group_id=176962&atid=879332
 */
public class LVE_MLocation extends MLocation implements DocAction
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8332515185354248079L;

	// http://jira.idempiere.com/browse/IDEMPIERE-147
	public static String LOCATION_MAPS_URL_PREFIX     = MSysConfig.getValue("LOCATION_MAPS_URL_PREFIX");
	public static String LOCATION_MAPS_ROUTE_PREFIX   = MSysConfig.getValue("LOCATION_MAPS_ROUTE_PREFIX");
	public static String LOCATION_MAPS_SOURCE_ADDRESS      = MSysConfig.getValue("LOCATION_MAPS_SOURCE_ADDRESS");
	public static String LOCATION_MAPS_DESTINATION_ADDRESS = MSysConfig.getValue("LOCATION_MAPS_DESTINATION_ADDRESS");
	public static final String COLUMNNAME_C_Municipality_ID = "C_Municipality_ID";
	public static final String COLUMNNAME_C_Parish_ID = "C_Parish_ID";
	
	
	
	/**
	 * 	Get Location from Cache
	 *	@param ctx context
	 *	@param C_Location_ID id
	 *	@param trxName transaction
	 *	@return MLocation
	 */
	public static LVE_MLocation get (Properties ctx, int C_Location_ID, String trxName)
	{
		//	New
		if (C_Location_ID == 0)
			return new LVE_MLocation(ctx, C_Location_ID, trxName);
		//
		Integer key = new Integer (C_Location_ID);
		LVE_MLocation retValue = null;
		if (trxName == null)
			retValue = (LVE_MLocation) s_cache.get (key);
		if (retValue != null)
			return retValue;
		retValue = new LVE_MLocation (ctx, C_Location_ID, trxName);
		if (retValue.get_ID () != 0)		//	found
		{
			if (trxName == null)
				s_cache.put (key, retValue);
			return retValue;
		}
		return null;					//	not found
	}	//	get

	/**
	 *	Load Location with ID if Business Partner Location
	 *	@param ctx context
	 *  @param C_BPartner_Location_ID Business Partner Location
	 *	@param trxName transaction
	 *  @return location or null
	 */
	public static LVE_MLocation getBPLocation (Properties ctx, int C_BPartner_Location_ID, String trxName)
	{
		if (C_BPartner_Location_ID == 0)					//	load default
			return null;

		LVE_MLocation loc = null;
		String sql = "SELECT * FROM C_Location l "
			+ "WHERE C_Location_ID IN (SELECT C_Location_ID FROM C_BPartner_Location WHERE C_BPartner_Location_ID=?)";
		try
		{
			PreparedStatement pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setInt(1, C_BPartner_Location_ID);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next())
				loc = new LVE_MLocation (ctx, rs, trxName);
			rs.close();
			pstmt.close();
		}
		catch (SQLException e)
		{
			s_log.log(Level.SEVERE, sql + " - " + C_BPartner_Location_ID, e);
			loc = null;
		}
		return loc;
	}	//	getBPLocation

	/**	Cache						*/
	private static CCache<Integer,LVE_MLocation> s_cache = new CCache<Integer,LVE_MLocation>("C_Location", 100, 30);
	/**	Static Logger				*/
	private static CLogger	s_log = CLogger.getCLogger(LVE_MLocation.class);

	
	/**************************************************************************
	 * 	Standard Constructor
	 *	@param ctx context
	 *	@param C_Location_ID id
	 *	@param trxName transaction
	 */
	public LVE_MLocation (Properties ctx, int C_Location_ID, String trxName)
	{
		super (ctx, C_Location_ID, trxName);
		if (C_Location_ID == 0)
		{
			MCountry defaultCountry = MCountry.getDefault(getCtx()); 
			setCountry(defaultCountry);
			MRegion defaultRegion = MRegion.getDefault(getCtx());
			if (defaultRegion != null 
				&& defaultRegion.getC_Country_ID() == defaultCountry.getC_Country_ID())
				setRegion(defaultRegion);
		}
	}	//	MLocation

	/**
	 * 	Parent Constructor
	 *	@param country mandatory country
	 *	@param region optional region
	 */
	public LVE_MLocation (MCountry country, MRegion region)
	{
		super (country.getCtx(), 0, country.get_TrxName());
		setCountry (country);
		setRegion (region);
	}	//	MLocation

	/**
	 * 	Full Constructor
	 *	@param ctx context
	 *	@param C_Country_ID country
	 *	@param C_Region_ID region
	 *	@param city city
	 *	@param trxName transaction
	 */
	public LVE_MLocation (Properties ctx, int C_Country_ID, int C_Region_ID, String city, String trxName)
	{
		super(ctx, 0, trxName);
		setC_Country_ID(C_Country_ID);
		setC_Region_ID(C_Region_ID);
		setCity(city);
	}	//	MLocation

	/**
	 * 	Load Constructor
	 *	@param ctx context
	 *	@param rs result set
	 *	@param trxName transaction
	 */
	public LVE_MLocation (Properties ctx, ResultSet rs, String trxName)
	{
		super(ctx, rs, trxName);
	}	//	MLocation

	private 	MCountry		m_c = null;
	private 	MRegion			m_r = null;
	private 	MMunicipality	m_m = null;
	private 	MParish			m_p = null;
	

	
	
	public void setMunicipality (MMunicipality municipality)
	{
		m_m = municipality;
		if (municipality == null)
		{
			this.setC_Municipality_ID(0);
			//set_Value (COLUMNNAME_C_Municipality_ID, null);
		}
		else
		{
			this.setC_Municipality_ID(m_m.getC_Municipality_ID());
			if (m_m.getC_Country_ID() != getC_Country_ID())
			{
				log.info("Municipality(" + municipality + ") C_Country_ID=" + municipality.getC_Country_ID()
						+ " - From  C_Country_ID=" + getC_Country_ID());
				setC_Country_ID(municipality.getC_Country_ID());
			}
		}
	}	//	setMunicipality
	public void setParish (MParish parish)
	{
		m_p = parish;
		if (parish == null)
		{
			this.setC_Parish_ID(0);
		}
		else
		{
			this.setC_Parish_ID(m_p.getC_Parish_ID());
			if (m_p.getC_Country_ID() != getC_Country_ID())
			{
				log.info("Parish(" + parish + ") C_Country_ID=" + parish.getC_Country_ID()
						+ " - From  C_Country_ID=" + getC_Country_ID());
				setC_Country_ID(parish.getC_Country_ID());
			}
		}
	}	//	setParish
	public void setC_Parish_ID(int C_Parish_ID) {
		if (C_Parish_ID < 1) 
			set_Value (COLUMNNAME_C_Parish_ID, null);
		else 
			set_Value (COLUMNNAME_C_Parish_ID, Integer.valueOf(C_Parish_ID));
		
	}
	
	
	public void setC_Municipality_ID(int C_Municipality_ID) {
		if (C_Municipality_ID < 1) 
			set_Value (COLUMNNAME_C_Municipality_ID, null);
		else 
			set_Value (COLUMNNAME_C_Municipality_ID, Integer.valueOf(C_Municipality_ID));
	}


	public MMunicipality getMunicipality()
	{
		// Reset municipality if not match
		if (m_m != null && m_m.get_ID() != getC_Municipality_ID())
			m_m = null;
		//
		if (m_m == null && getC_Municipality_ID() != 0)
			m_m = MMunicipality.get(getCtx(), getC_Municipality_ID());
		return m_m;
	}	
	public int getC_Municipality_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Municipality_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
	public MParish getParish()
	{
		// Reset parish if not match
		if (m_p != null && m_p.get_ID() != getC_Parish_ID())
			m_p = null;
		//
		if (m_p == null && getC_Parish_ID() != 0)
			m_p = MParish.get(getCtx(), getC_Parish_ID());
		return m_p;
	}	
	public int getC_Parish_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Parish_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
	

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
}	//	MLocation
