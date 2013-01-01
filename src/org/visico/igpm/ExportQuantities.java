package org.visico.igpm;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;

import org.bimserver.interfaces.objects.SDataObject;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;

public class ExportQuantities 
{
	public static void main(String[] args) throws ServerException, UserException
	{
		
	}
	
	public static void queryAll() throws ServerException, UserException, WriteException, IOException, BiffException
	{
		QueryMain m = new QueryMain();
		m.connectService();
		
		
		HashSet<Storey> storeys = m.getStoreysFromServer(projectName, null);
		
		// reads and writes Excel spreadsheet from demo folder
		// change file to your liking
		ExcelSheet sheet = new ExcelSheet("demo/Quantities.xls", 
				"demo/Quantities_new.xls",
				"Quantities");
		
		Iterator<Storey> it = storeys.iterator();
		while (it.hasNext())
		{
			Storey s = it.next();
			List<SDataObject> walls = s.getObjectsByType("IfcWallStandardCase");
			
			Iterator<SDataObject> wallit = walls.iterator();
			
			
			while (wallit.hasNext())
			{
				SDataObject wall = wallit.next();
				QuantityObject qo = new QuantityObject(wall, s);
				qo.printProperties();
				qo.exportAllProperties(sheet);	
			}
		}
		
		sheet.writeAndClose();
	}
	
	
	// this uses the example project name, please exchange it with the name of your group's project
	private static final String projectName = "Tubantia";
}
