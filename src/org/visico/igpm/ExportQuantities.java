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
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.shared.ServiceInterface;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;

public class ExportQuantities 
{
	public static void main(String[] args) throws ServerException, UserException, RowsExceededException, BiffException, WriteException, IOException
	{
		queryAll();
		//queryAllByStorey();
		//exportWallAreaPerPhase();
	}
	
	
	public static void queryAll() throws ServerException, UserException 
	{
		QueryMain m = new QueryMain();
		m.connectService();
		
		ServiceInterface service = m.getService();
		SProject project = m.getProject(projectName);
		
		long revisionId = project.getLastRevisionId();
    	
    	// get storey elements
       List<SDataObject> objects = new ArrayList<SDataObject>();
       
       List<SDataObject> temp = service.getDataObjectsByType(revisionId, "IfcDoor");
       if (temp != null)
    	   objects.addAll(temp);
       
       temp = service.getDataObjectsByType(revisionId, "IfcColumn");
       if (temp != null)
    	   objects.addAll(temp);
       
       temp = service.getDataObjectsByType(revisionId, "IfcRoof");
       if (temp != null)
    	   objects.addAll(temp);
       
       temp = service.getDataObjectsByType(revisionId, "IfcStair");
       if (temp != null)
    	   objects.addAll(temp);
       
       temp = service.getDataObjectsByType(revisionId, "IfcWindow");
       if (temp != null)
    	   objects.addAll(temp);
       
       temp = service.getDataObjectsByType(revisionId, "IfcSlab");
       if (temp != null)
    	   objects.addAll(temp);
       
       temp = service.getDataObjectsByType(revisionId, "IfcWallStandardCase");
       if (temp != null)
    	   objects.addAll(temp);
       	
			
			Iterator<SDataObject> objectIt = objects.iterator();
			
			while (objectIt.hasNext())
			{
				SDataObject object = objectIt.next();
				ObjectContainer container = new ObjectContainer(object, service, revisionId);
				
				PropertyObject qo = new PropertyObject(object, container);
				qo.printProperties();
			}
		
		
		
	}

	// will only work if storey objects are linked to other objcets; 
	// check by browsing the storey object on the ifc server
	public static void queryAllByStorey() throws ServerException, UserException 
	{
		QueryMain m = new QueryMain();
		m.connectService();
		
		HashSet<ObjectContainer> storeys = m.getStoreysFromServer(projectName, null);
		
		Iterator<ObjectContainer> it = storeys.iterator();
		while (it.hasNext())
		{
			ObjectContainer s = it.next();
			List<SDataObject> objects = s.getObjectsByType("IfcDoor");
			objects.addAll(s.getObjectsByType("IfcColumn"));
			objects.addAll(s.getObjectsByType("IfcRoof"));
			objects.addAll(s.getObjectsByType("IfcStair"));
			objects.addAll(s.getObjectsByType("IfcWindow"));
			objects.addAll(s.getObjectsByType("IfcSlab"));
			objects.addAll(s.getObjectsByType("IfcWallStandardCase"));
			
			
			Iterator<SDataObject> objectIt = objects.iterator();
			
			while (objectIt.hasNext())
			{
				SDataObject object = objectIt.next();
				PropertyObject qo = new PropertyObject(object, s);
				qo.printProperties();
			}
		}
		
		
	}
	
	
	public static void exportWallAreaPerPhase() throws ServerException, UserException, BiffException, IOException, RowsExceededException, WriteException
	{
		QueryMain m = new QueryMain();
		m.connectService();
		
		HashSet<ObjectContainer> storeys = m.getStoreysFromServer(projectName, null);
		ExcelSheet sheet = new ExcelSheet("demo/phases.xls", "demo/phases_new.xls", "Quantities");
		
		// get first storey object
		// again this will only work if the storey links its objects, check the BIM server
		Iterator<ObjectContainer> it = storeys.iterator();
		ObjectContainer s = it.next();
		
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
					lengthPerPhase.put(phase, aggregatedLength);
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
	private static final String projectName = "Test";
}
