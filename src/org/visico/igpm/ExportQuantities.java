package org.visico.igpm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bimserver.client.ClientIfcModel;
import org.bimserver.models.ifc2x3tc1.IfcBuildingStorey;
import org.bimserver.models.ifc2x3tc1.IfcColumn;
import org.bimserver.models.ifc2x3tc1.IfcDoor;
import org.bimserver.models.ifc2x3tc1.IfcObject;
import org.bimserver.models.ifc2x3tc1.IfcRelContainedInSpatialStructure;
import org.bimserver.models.ifc2x3tc1.IfcRoof;
import org.bimserver.models.ifc2x3tc1.IfcSlab;
import org.bimserver.models.ifc2x3tc1.IfcStair;
import org.bimserver.models.ifc2x3tc1.IfcWall;
import org.bimserver.models.ifc2x3tc1.IfcWallStandardCase;
import org.bimserver.models.ifc2x3tc1.IfcWindow;
import org.bimserver.plugins.services.BimServerClientException;
import org.bimserver.shared.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;

import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;


public class ExportQuantities 
{
	// this uses the example project name, please exchange it with the name of your group's project
	private static final String projectName = "Test";
	private static ExcelSheet sheet;

	public static void main(String[] args) throws ServerException, UserException, RowsExceededException, BiffException, WriteException, IOException, BimServerClientException, PublicInterfaceNotFoundException
	{
		sheet = new ExcelSheet("demo/phases.xls", "demo/phases_new.xls", "Quantities");

		//implement a timer
		Long startTime = new Long(System.currentTimeMillis());

		queryAll();
		queryAllByStorey();
		exportWallAreaPerPhase();

		Long time = (System.currentTimeMillis() - startTime) / 1000;
		System.out.println("Overall duration "+ time.toString() + " seconds!");

		sheet.writeAndClose();
	}


	public static void queryAll() throws ServerException, UserException, BimServerClientException, PublicInterfaceNotFoundException 
	{
		//implement a timer
		Long startTime = new Long(System.currentTimeMillis());

		QueryMain m = new QueryMain();
		ClientIfcModel model = m.getModel(projectName);

		List<IfcObject> objects = new ArrayList<IfcObject>();

		objects.addAll(model.getAllWithSubTypes(IfcWallStandardCase.class));
		objects.addAll(model.getAllWithSubTypes(IfcColumn.class));
		objects.addAll(model.getAllWithSubTypes(IfcRoof.class));
		objects.addAll(model.getAllWithSubTypes(IfcStair.class));
		objects.addAll(model.getAllWithSubTypes(IfcWindow.class));
		objects.addAll(model.getAllWithSubTypes(IfcSlab.class));
		objects.addAll(model.getAllWithSubTypes(IfcColumn.class));
		objects.addAll(model.getAllWithSubTypes(IfcWall.class));
		objects.addAll(model.getAllWithSubTypes(IfcDoor.class));

		Iterator<IfcObject> objectIt = objects.iterator();

		while (objectIt.hasNext())
		{
			IfcObject object = objectIt.next();

			PropertyObject qo = new PropertyObject(object);
			qo.printProperties();
		}

		Long time = (System.currentTimeMillis() - startTime) / 1000;
		System.out.println("query took "+ time.toString() + " seconds!");

	}

	public static void queryAllByStorey() throws ServerException, UserException, BimServerClientException, PublicInterfaceNotFoundException 
	{
		//implement a timer
		Long startTime = new Long(System.currentTimeMillis());

		QueryMain m = new QueryMain();
		ClientIfcModel model = m.getModel(projectName);

		List<IfcBuildingStorey> objects = model.getAllWithSubTypes(IfcBuildingStorey.class);

		for (IfcBuildingStorey storey : objects)
		{
			System.out.println();
			System.out.println();
			System.out.println(storey.getName());

			List<IfcRelContainedInSpatialStructure> contained = storey.getContainsElements();
			for (IfcRelContainedInSpatialStructure c : contained)
			{
				for (IfcObject el : c.getRelatedElements())
				{
					PropertyObject qo = new PropertyObject(el);
					qo.printProperties();
				}
			}
		}

		Long time = (System.currentTimeMillis() - startTime) / 1000;
		System.out.println("query took "+ time.toString() + " seconds!");
	}

	public static void exportWallAreaPerPhase() throws ServerException, UserException, BiffException, IOException, RowsExceededException, WriteException, BimServerClientException, PublicInterfaceNotFoundException
	{
		//implement a timer
		Long startTime = new Long(System.currentTimeMillis());

		QueryMain m = new QueryMain();
		ClientIfcModel model = m.getModel(projectName);

		List<IfcBuildingStorey> objects = model.getAllWithSubTypes(IfcBuildingStorey.class);

		// get the first storey object
		IfcBuildingStorey storey = objects.get(0);

		System.out.println();
		System.out.println();
		System.out.println(storey.getName());

		// create the map to hold the length per phase
		HashMap<String, Double> lengthPerPhase = new HashMap<String, Double>();

		// get the first storey element 
		List<IfcRelContainedInSpatialStructure> contained = storey.getContainsElements();
		for (IfcRelContainedInSpatialStructure c : contained)
		{
			for (IfcObject el : c.getRelatedElements())
			{
				// select only walls
				if (el instanceof IfcWallStandardCase || el instanceof IfcWall)
				{
					PropertyObject qo = new PropertyObject(el);

					String phase = qo.getQuantity("Phase Created");
					String lengthStr = qo.getQuantity("Length");
					
					// no property set, ignore wall
					if (phase != null && lengthStr != null)
					{
						Double length = Double.parseDouble(lengthStr);
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

		Long time = (System.currentTimeMillis() - startTime) / 1000;
		System.out.println("query took "+ time.toString() + " seconds!");
	}



}
