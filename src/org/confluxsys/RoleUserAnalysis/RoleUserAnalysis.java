package org.confluxsys.RoleUserAnalysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileLock;
import java.nio.channels.NonWritableChannelException;
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
		String roleDefinationUrl = null;
		String userAccessUrl = null;
		String suggestedRoleUrl = null;
		String orphanEntitlementsUrl = null;
		
		LinkedHashMap<String, List<String>> roleDefinations = null;
		LinkedHashMap<String, List<String>> userAccess = null;
		LinkedHashMap<String, List<String>> userRole = null;
		
		Exception check = null;
		FileInputStream fis = null;
		FileLock islock = null;

		RoleUserAnalysis rul = new RoleUserAnalysis();				//Creating Object of class RoleUserAnalysis
		Properties filename = new Properties();						//creating object of Properties to access properties file
		
		try
		{
			fis = new FileInputStream("filename.properties");
			islock = fis.getChannel().tryLock();					//locking filename.properties
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Cannot Find filename.properties");
			check = e;
		}
		catch(IOException e)
		{
			check = e;
			System.out.println("IOException Encountered During Processing Of filename.properties");
		}
		catch(NonWritableChannelException e){}
		
		try
		{
			filename.load(fis);										//Loading FileInputStream in Properties object
		}
		catch(IOException e1)
		{
			check = e1;
			System.out.println("IOException Encountered During Loading Of filename.properties");
		}
		finally
		{
			//Stop Execution if Exception occurs
			if(check != null)
			{
				System.gc();
				System.exit(0);
			}
		}
		
		//fetching file url from filename.properties
		roleDefinationUrl = filename.getProperty("roleDefinationUrl");
		userAccessUrl = filename.getProperty("userAccessUrl");
		suggestedRoleUrl = filename.getProperty("suggestedRoleUrl");
		orphanEntitlementsUrl = filename.getProperty("orphanEntitlementsUrl");
		
		//realising Lock of the File config.properties
		if(islock != null)
		{
			try
			{
				islock.release();
				islock.close();
				
				//closing FileInputStream Object
				if(fis != null)
				{
					fis.close();
				}
			}
			catch (IOException e)
			{
				System.out.println("IOException Encountered");
			}
		}
		
		//calling and passing file URL to readxlsx(url) method
		roleDefinations = rul.readxlsx(roleDefinationUrl);
		
		//calling and passing file URL to readxlsx(url) method
		userAccess = rul.readxlsx(userAccessUrl);
	
		//calling suggestedRole(roleDefinations, userAccess) to map userAccess and roleDefination and find orphan group
		userRole = rul.suggestedRole(roleDefinations, userAccess);
		
		//calling toExcel to store Suggested Role and Orphan Group in Excel Sheet
		rul.toExcel(userRole, suggestedRoleUrl, orphanEntitlementsUrl);
		
		System.out.println("Role suggestions saved in " + suggestedRoleUrl);
		System.out.println("Orphan Entitlements saved in " + orphanEntitlementsUrl);
	}
	
	//Converting excell file into Data structure using LinkedHashMap
	private LinkedHashMap<String, List<String>> readxlsx(String url)
	{
		FileInputStream fis = null;
		Exception check = null;
		FileLock islock = null;
		XSSFWorkbook wb = null;
		XSSFSheet sheet = null;
		
		LinkedHashMap<String, List<String>> hm = new LinkedHashMap<String, List<String>>();		//to store the xlsx file for processing		
		ArrayList<String> groups = new ArrayList<String>();			//to store the groups names and number of groups
		
		//obtaining input bytes from file
		try
		{
			fis = new FileInputStream(new File(url));
			islock = fis.getChannel().tryLock();					//Locking Input File
		}
		catch(FileNotFoundException e)
		{
			System.out.println("File Not Present.\nPlease Check Input Filenames in filename.properties.");
			check = e;
		}
		catch(IOException e)
		{
			System.out.println("IOException Encountered During Processing Of Input File.");
			check = e;
		}
		catch(NonWritableChannelException e)
		{}
		
		try
		{
			//creating workbook instance
			wb = new XSSFWorkbook(fis);
		}
		catch(IOException e1)
		{
			check = e1;
			System.out.println("IOException Encountered During creating instance of XSSFWorkbook");
		}
		finally
		{
			//close open objects
			try
			{
				if(fis != null)
					fis.close();
				if(wb != null)
					wb.close();
			}
			catch(IOException e)
			{
				System.out.println("IOException Encountered During closing open objects.");
				check = e;
			}
			
			//Stop Execution if Exception Occurs
			if(check != null)
			{
				//releasing the lock on Input File if some Exception occures
				if(islock != null)
				{
					try
					{
						islock.release();
						islock.close();
					}
					catch (IOException e)
					{
						System.out.println("IOException Encountered During releasing lock on Input File.");
					}
				}
				System.gc();
				System.exit(0);
			}
		}
		
		sheet = wb.getSheetAt(0);
		
		for(Row row : sheet)								//loop for each row
		{	
			List<String> li = new ArrayList<String>();		// to store the Groups of individual
			Cell cell;
			
			if(row.getRowNum() == 0)
				for(Cell c : row)
					groups.add(c.getStringCellValue());		//adding the group name to arraylist
			else
			{
				String key="";
				String s = "";
				for(int i =0; i<groups.size(); i++)				//loop for each cell
				{
					cell = row.getCell(i);
					if(cell != null)							//check if the cell is empty
					{
						s = cell.getStringCellValue();	//convert cell type to String type
						if(i==0)
							key = s;							//storing keys
						else
							li.add(groups.get(i));				//adding in list
					}
				}
				hm.put(key, li);								//adding in LinkedHashMap
			}
		}
		
		//relesing the lock on Input Files after normal execution
		if(islock != null)
		{
			try
			{
				islock.release();
				islock.close();
			}
			catch (IOException e)
			{
				System.out.println("IOException Encountered During releasing lock on Input File.");
				check = e;
			}
		}
		
		//returning the data in the file as a LinkedHashMap
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
			int maxcomparisionfactor = 0;						//for Storing the best Match value
			boolean flag = false;								//flag for a perfect match
			
			String roleSuggest = "";							//for saving suggestied role after comparisions
			
			List<String> orphan = new ArrayList<String>();		//for saving the orphan group
			List<String> roleOrphan = new ArrayList<String>();
			List<String> tempOrphan = new ArrayList<String>();

			List<String> uaList = ua.get(uakey);				//extracting UserAccess Group List
			
			
			for(String rdkey : rdkeys)							//loop for each Role Defination Keys
			{
				int comparisionfactor=0;						//for storing current row match value
				
				List<String> matched = new ArrayList<String>();	//to check matched groups to calculate orphan
				
				List<String> rdList = rd.get(rdkey);			//extracting RoleDefination Group List

				tempOrphan.clear();
				
				if(uaList.containsAll(rdList))
				{
					roleSuggest = rdkey;						//if lists match exactly then save the role and break
					for(String uaGroup : uaList)				//to check orphan group of User for best match
						if(!rdList.contains(uaGroup))
							tempOrphan.add(uaGroup);
					flag = true;								//flaging for best match
					break;										//getting out of the for loop of Rows if best match found
				}
				else											//logic to  calculate variable of comparision
				{
					for(String rdGroup : rdList)				//loop for traversing each group in Role Defination List
					{					
						if(uaList.contains(rdGroup))			//checking each group of roles with UserAccess list
						{
							matched.add(rdGroup);				//if role matched add in matched ArrayList
							comparisionfactor++;				
						}
						else
							comparisionfactor--;
					}
					
					//to get the orphan group for the current user from the current row and save in tempOrphan ArrayList
					for(String uaGroup : uaList)				
						if(!matched.contains(uaGroup))
							tempOrphan.add(uaGroup);
				}
				
				//if current row has higher comparisionfactor then make it maxcomparisionfactor and save the row and orphan group array
				if(comparisionfactor >= maxcomparisionfactor)		
				{
					maxcomparisionfactor = comparisionfactor;
					roleSuggest = rdkey;
					orphan = tempOrphan;
				}
			}
			//if perfect match was found then save its orphan groups
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
		int rowId = 0;											//defining rowId to traverse rows of Excel
        int cellId = 0;											//defining cellId to traverse cells of Excel
        
        File suggestedRole = null;
		File orphanEntitlement = null;
        FileOutputStream out = null;
		FileLock islock = null;
		Exception check = null;
		
        Cell cellSuggestedRole;
        Cell cellOrphanEntitlements;
		
		Set<String> keyId = userRole.keySet();
		
		//Creating Blank WorkBook
		XSSFWorkbook wbSuggestedRole = new XSSFWorkbook();
		XSSFWorkbook wbOrphanEntitlements = new XSSFWorkbook();
		
		//Creating Blank Sheet
		XSSFSheet sheetSuggestedRole = wbSuggestedRole.createSheet("SuggestedRoleSheet");
		XSSFSheet sheetOrphanEntitlements = wbOrphanEntitlements.createSheet("OrphanEntitlementsSheet");
		
		//Creating 1st Row for both excel sheets for storing title
		XSSFRow rowSuggestedRole = sheetSuggestedRole.createRow(rowId);
		XSSFRow rowOrphanEntitlements = sheetOrphanEntitlements.createRow(rowId);
		
        rowId++;
        
        while(cellId < 2)										//Defining 1st row of both excel sheet
        {
        	//Creating Cell for 1st row for both excel sheets (storing title)
	        cellSuggestedRole = rowSuggestedRole.createCell(cellId);
			cellOrphanEntitlements = rowOrphanEntitlements.createCell(cellId);
		
			if(cellId == 0)										//Defining 1st Cell of 1st row of both excel sheet
			{
				cellSuggestedRole.setCellValue("User");
				cellOrphanEntitlements.setCellValue("User");
			}
			if(cellId ==1)										//Defining 2nd Cell of 1st row of both excel sheet
			{
				cellSuggestedRole.setCellValue("Role");
				cellOrphanEntitlements.setCellValue("Orphan");
			}
			cellId++;
		}
        
		for (String key : keyId)
		{
			 cellId = 0;											//redefining cellId to 0 for each row
			 String str = "";
			
			 List<String> roleOrphanList= userRole.get(key);			//Extracting the List value for each key(user)
			 
			 //Creating Row in Excel Sheet
	         rowSuggestedRole = sheetSuggestedRole.createRow(rowId);
	         rowOrphanEntitlements = sheetOrphanEntitlements.createRow(rowId);
	         rowId++;
	         
	         //Creating Cell for both excel sheets
	         cellSuggestedRole = rowSuggestedRole.createCell(cellId);
    		 cellOrphanEntitlements = rowOrphanEntitlements.createCell(cellId);
    		 cellId++;
    		 
    		//Setting first cell into both excel sheets
    		 cellSuggestedRole.setCellValue(key);
    		 cellOrphanEntitlements.setCellValue(key);
    		 
    		 //setting 2nd cell into SuggestedRole excel sheet
        	 str = roleOrphanList.get(0);
    		 cellSuggestedRole = rowSuggestedRole.createCell(cellId);
    		 cellSuggestedRole.setCellValue(str);
    		 
    		 //setting 2nd cell into OrphanEntitlements excel sheet
    		 for(int i=1; (i<roleOrphanList.size()); i++)
    		 {
	    		 str = roleOrphanList.get(i);
	        	 cellOrphanEntitlements = rowOrphanEntitlements.createCell(cellId);
	        	 cellOrphanEntitlements.setCellValue(str);
	        	 cellId++;
    		 }
	    }
		
		//Writing the workbook in files
		
		suggestedRole = new File(suggestedRoleUrl);
		orphanEntitlement = new File(orphanEntitlementsUrl);
		
		try
		{
			if(!suggestedRole.exists())
				suggestedRole.createNewFile();
			if(!orphanEntitlement.exists())
				orphanEntitlement.createNewFile();
			
			//writing in File suggestedRole
			out = new FileOutputStream(suggestedRole);
			islock = out.getChannel().tryLock();				//locking file suggestedRole

			wbSuggestedRole.write(out);							//copying WorkBook data into File
			
			//releasing the lock on suggestedRole
			if(islock != null)
			{
				try
				{
					islock.release();
					islock.close();
				}
				catch(ClosedChannelException e){}
				catch (IOException e)
				{
					System.out.println("IOException Encountered during releasing lock on Output File");
					check = e;
				}
			}
				
			//closing FileOutputStream object of suggestedRole
			if(out != null)
				out.close();
			//closing WSSFWorkbook object of suggestedRole
			if(wbSuggestedRole != null)
				wbSuggestedRole.close();
			
			//writing in File orphanEntitlement
			out = new FileOutputStream(orphanEntitlement);
			islock = out.getChannel().tryLock();				//locking file orphanEntitlement
			
			wbOrphanEntitlements.write(out);						//copying WorkBook data into File

			//releasing the lock on suggestedRole
			if(islock != null)
			{
				try
				{
					islock.release();
					islock.close();
				}
				catch(ClosedChannelException e){}
				catch(IOException e)
				{
					System.out.println("IOException Encountered during releasing lock on Output File");
					check = e;
				}
			}
			
			//closing FileOutputStream object of orphanEntitlement
			if(out != null)
				out.close();
			//closing WSSFWorkbook object of suggestedRole
			if(wbOrphanEntitlements != null)
				wbOrphanEntitlements.close();
			
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Output File Open In Another Application");
			check = e;
		}
		catch(IOException e)
		{
			System.out.println("IOException Encountered During Processing Of Output File");
			check = e;
		}
		
		if(check != null)
		{
			System.gc();
			System.exit(0);
		}
	}
}
