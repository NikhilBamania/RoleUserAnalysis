package org.confluxsys.RoleUserAnalysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RoleUserAnalysis {

	public static void main(String[] args)
	{
		//Creating Object of class RoleUserAnalysis
		RoleUserAnalysis rul = new RoleUserAnalysis();
		
		//using properties file
		Properties filename = new Properties();
		try
		{
			filename.load(new FileInputStream("filename.properties")); 
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		//fetching file url from properties file
		String roleDefinationUrl = filename.getProperty("roleDefinationUrl");
		String userAccessUrl = filename.getProperty("userAccessUrl");
		String suggestedRoleUrl = filename.getProperty("suggestedRoleUrl");
		String orphanEntitlementsUrl = filename.getProperty("orphanEntitlementsUrl");
		
		/*
		//stroing all file Urls in String
		String roleDefinationUrl = "Role_Definitions.xlsx";
		String userAccessUrl = "User_Access.xlsx";
		String suggestedRoleUrl = "SuggestedRole.xlsx";
		String orphanEntitlementsUrl = "OrphanEntitlements.xlsx";
		*/
		
		//calling and passing file URL to readxlsx(url) method
		LinkedHashMap<String, List<String>> roleDefinations = rul.readxlsx(roleDefinationUrl);
		
		//calling and passing file URL to readxlsx(url) method
		LinkedHashMap<String, List<String>> userAccess = rul.readxlsx(userAccessUrl);
	
		//calling suggestedRole(roleDefinations, userAccess) to map userAccess and roleDefination and find orphan group
		LinkedHashMap<String, List<String>> userRole = rul.suggestedRole(roleDefinations, userAccess);
		
		//calling toExcel to store Suggested Role and Orphan Group in Excel Sheet
		rul.toExcel(userRole, suggestedRoleUrl, orphanEntitlementsUrl);
		
		System.out.println("Role suggestions saved in " + suggestedRoleUrl);
		System.out.println("Orphan Entitlements saved in " + orphanEntitlementsUrl);
	}
	
	//Converting excell file into Data structure using LinkedHashMap
	private LinkedHashMap<String, List<String>> readxlsx(String url)
	{
		//to store the xlsx file for processing
		LinkedHashMap<String, List<String>> hm = new LinkedHashMap<String, List<String>>();
		//to store the groups names and number of groups
		ArrayList<String> groups = new ArrayList<String>();
		
		FileInputStream f = null;
		XSSFWorkbook wb = null;
		//obtaining input bytes from file
		try
		{
			f = new FileInputStream(new File(url));
			//creating workbook instance
			wb = new XSSFWorkbook(f);
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(f != null)
					f.close();
				if(wb != null)
					wb.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
		XSSFSheet sheet = wb.getSheetAt(0);
			
		for(Row row : sheet)								//loop for each row
		{	
			List<String> li = new ArrayList<String>();		// to store the Groups of individual
			if(row.getRowNum() == 0)
				for(Cell cell : row)
					groups.add(cell.getStringCellValue());		//adding the group name to arraylist
			else
			{
				String key="";
				for(int i =0; i<groups.size(); i++)				//loop for each cell
				{
					Cell cell = row.getCell(i);
					if(cell != null)							//check if the cell is empty
					{
						String s = cell.getStringCellValue();	//convert cell type to String type
						if(i==0)
							key = s;							//storing keys
						else
							li.add(groups.get(i));				//adding in list
					}
				}
				hm.put(key, li);								//adding in LinkedHashMap
			}
		}
		return hm;
	}
	
	
	//finding Role of the User and orphan group
	private LinkedHashMap<String, List<String>> suggestedRole(LinkedHashMap<String, List<String>> rd, LinkedHashMap<String, List<String>> ua)
	{
		LinkedHashMap<String, List<String>> userRole = new LinkedHashMap<String, List<String>>();
		Set<String> uakeys= ua.keySet();						//KeySet of UserAccess
		Set<String> rdkeys = rd.keySet();						//KeySet of RoleDefination
		for(String uakey: uakeys)								//loop for each Users Keys
		{
			String roleSuggest = "";							//for saving suggestied role after comparisions
			List<String> orphan = new ArrayList<String>();		//for saving the orphan group
			List<String> uaList = ua.get(uakey);				//extracting UserAccess Group List
			List<String> roleOrphan = new ArrayList<String>();
			List<String> tempOrphan = new ArrayList<String>();
			
			int maxcomparisionfactor = 0;						//for comparing rows match
			boolean flag = false;
			for(String rdkey : rdkeys)							//loop for each Role Defination Keys
			{
				int comparisionfactor=0;						//variable of comparision
				tempOrphan.clear();
				List<String> matched = new ArrayList<String>();	//to check matched groups to calculate orphan
				List<String> rdList = rd.get(rdkey);			//extracting RoleDefination Group List
				if(uaList.containsAll(rdList))
				{
					roleSuggest = rdkey;						//if lists match exactly then save the role and break
					for(String uaGroup : uaList)
						if(!rdList.contains(uaGroup))
							tempOrphan.add(uaGroup);
					flag = true;
					break;
				}
				else										//logic to  calculate variable of comparision
				{
					for(String rdGroup : rdList)				//loop for traversing each group in Role Defination List
					{					
						if(uaList.contains(rdGroup))
						{
							matched.add(rdGroup);
							comparisionfactor++;
						}
						else
							comparisionfactor--;
					}
					for(String uaGroup : uaList)
						if(!matched.contains(uaGroup))
							tempOrphan.add(uaGroup);
				}
				if(comparisionfactor >= maxcomparisionfactor)
				{
					maxcomparisionfactor = comparisionfactor;
					roleSuggest = rdkey;
					orphan = tempOrphan;
				}
			}
			if(flag)
				orphan = tempOrphan;
			roleOrphan.add(roleSuggest);
			roleOrphan.addAll(orphan);
			userRole.put(uakey, roleOrphan);
		}
		return userRole;
	}
	
	
	private void toExcel(LinkedHashMap<String, List<String>> userRole, String suggestedRoleUrl, String orphanEntitlementsUrl)
	{
		//Creating Blank WorkBook
		XSSFWorkbook wbSuggestedRole = null;
		wbSuggestedRole = new XSSFWorkbook();
		XSSFWorkbook wbOrphanEntitlements = null;
		wbOrphanEntitlements = new XSSFWorkbook();
		
		//Creating Blank Sheet
		XSSFSheet sheetSuggestedRole = wbSuggestedRole.createSheet("SuggestedRoleSheet");
		XSSFSheet sheetOrphanEntitlements = wbOrphanEntitlements.createSheet("OrphanEntitlementsSheet");
		
		XSSFRow rowSuggestedRole;
		XSSFRow rowOrphanEntitlements;
		
		Set<String> keyId = userRole.keySet();
		int rowId = 0;
		
		//Defining Title Row for both excel sheets
		rowSuggestedRole = sheetSuggestedRole.createRow(rowId);
        rowOrphanEntitlements = sheetOrphanEntitlements.createRow(rowId);
        rowId++;
        int cellId = 0;											//defining cellId to track cells of Excel
        
        //Creating Cell for both excel sheets
        Cell cellSuggestedRole = rowSuggestedRole.createCell(cellId);
		Cell cellrowOrphanEntitlements = rowOrphanEntitlements.createCell(cellId);
		cellId++;
		cellSuggestedRole.setCellValue("User");
		cellrowOrphanEntitlements.setCellValue("User");
		
		cellSuggestedRole = rowSuggestedRole.createCell(cellId);
		cellrowOrphanEntitlements = rowOrphanEntitlements.createCell(cellId);
		cellSuggestedRole.setCellValue("Role");
		cellrowOrphanEntitlements.setCellValue("Orphan");
		for (String key : keyId)
		{
			//Creating Row in Excel Sheet
	         rowSuggestedRole = sheetSuggestedRole.createRow(rowId);
	         rowOrphanEntitlements = sheetOrphanEntitlements.createRow(rowId);
	         rowId++;
	         
	         List<String> roleOrphanList= userRole.get(key);			//Extracting the List value for each key(user)
	         cellId = 0;											//redefining cellId to 0 for each row
	         
	         //Creating Cell for both excel sheets
	         cellSuggestedRole = rowSuggestedRole.createCell(cellId);
    		 cellrowOrphanEntitlements = rowOrphanEntitlements.createCell(cellId);
    		 cellId++;
    		 
    		//Setting first cell into both excel sheets
    		 cellSuggestedRole.setCellValue(key);
    		 cellrowOrphanEntitlements.setCellValue(key);
    		 
    		 //setting 2nd cell into SuggestedRole excel sheet
        	 String str = roleOrphanList.get(0);
    		 cellSuggestedRole = rowSuggestedRole.createCell(cellId);
    		 cellSuggestedRole.setCellValue(str);
    		 
    		 //setting 2nd cell into OrphanEntitlements excel sheet
    		 for(int i=1; (i<roleOrphanList.size()); i++)
    		 {
	    		 str = roleOrphanList.get(i);
	        	 cellrowOrphanEntitlements = rowOrphanEntitlements.createCell(cellId);
	        	 cellrowOrphanEntitlements.setCellValue(str);
	        	 cellId++;
    		 }
	      }
		
			//Writing the workbook in file system
			File suggestedRole = new File(suggestedRoleUrl);
			File orphanEntitlement = new File(orphanEntitlementsUrl);
			FileOutputStream out = null;
			try
			{
				if(!suggestedRole.exists())
					suggestedRole.createNewFile();
				if(!orphanEntitlement.exists())
					orphanEntitlement.createNewFile();
				
				out = new FileOutputStream(suggestedRole);
				wbSuggestedRole.write(out);
				out = new FileOutputStream(orphanEntitlement);
				wbOrphanEntitlements.write(out);
				if(out != null)
					out.close();
				if(wbOrphanEntitlements != null)
					wbOrphanEntitlements.close();
				if(wbSuggestedRole != null)
					wbSuggestedRole.close();
			}
			catch(FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
	}
}
