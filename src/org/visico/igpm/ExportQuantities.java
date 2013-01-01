package org.visico.igpm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.bimserver.interfaces.objects.SDataObject;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;

public class ExportQuantities 
{
	public static void main(String[] args) throws ServerException, UserException, RowsExceededException, BiffException, WriteException, IOException
	{
		//queryAll();
		exportWallAreaPerPhase();
	}
	
	public static void queryAll() throws ServerException, UserException 
	{
		QueryMain m = new QueryMain();
		m.connectService();
		
		HashSet<Storey> storeys = m.getStoreysFromServer(projectName, null);
		
		Iterator<Storey> it = storeys.iterator();
		while (it.hasNext())
		{
			Storey s = it.next();
			List<SDataObject> walls = s.getObjectsByType("IfcWallStandardCase");
			
			Iterator<SDataObject> wallit = walls.iterator();
			
			while (wallit.hasNext())
			{
				SDataObject wall = wallit.next();
				PropertyObject qo = new PropertyObject(wall, s);
				qo.printProperties();
			}
		}
		
		
	}
	
	
	public static void exportWallAreaPerPhase() throws ServerException, UserException, BiffException, IOException, RowsExceededException, WriteException
	{
		QueryMain m = new QueryMain();
		m.connectService();
		
		HashSet<Storey> storeys = m.getStoreysFromServer(projectName, null);
		ExcelSheet sheet = new ExcelSheet("demo/phases.xls", "demo/phases_new.xls", "Quantities");
		
		// get first storey object
		Iterator<Storey> it = storeys.iterator();
		Storey s = it.next();
		
		HashMap<String, Double> lengthPerPhase = new HashMap<String, Double>();
		List<SDataObject> walls = s.getObjectsByType("IfcWallStandardCase");
		Iterator<SDataObject> wallit = walls.iterator();
		
		while (wallit.hasNext())
		{
			SDataObject wall = wallit.next();
			PropertyObject qo = new PropertyObject(wall, s);
			
			String phase = qo.getQuantity("Phase Created");
			String lengthStr = qo.getQuantity("Length");
			Double length = Double.parseDouble(lengthStr);
			
			// no property set, ignore wall
			if (phase != null)
			{
				// already a wall with this phase: add length to value
				if (lengthPerPhase.containsKey(phase))
				{
					Double aggregatedLength = lengthPerPhase.get(phase);
					aggregatedLength = aggregatedLength + length;
				}
				// create new HashMap entry
				else
				{
					lengthPerPhase.put(phase, length);
				}
			}		
		}
		
		// write HashMap to Excel
		Iterator<String> keyIt = lengthPerPhase.keySet().iterator();
		
		//start at row 2 to spare heading
		int row = 2;
		while (keyIt.hasNext())
		{
			String phase = keyIt.next();
			Label labelCell = new Label(1, row, phase);
			sheet.getSheet().addCell(labelCell);
			
			Number number = new Number(2, row, lengthPerPhase.get(phase).doubleValue());
			sheet.getSheet().addCell(number);
			row++;
		}
		
		sheet.writeAndClose();
	}
	
	// this uses the example project name, please exchange it with the name of your group's project
	private static final String projectName = "Tubantia";
}
