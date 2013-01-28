package org.visico.igpm;


import java.io.IOException;
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
import org.bimserver.interfaces.objects.SDataValue;
import org.bimserver.interfaces.objects.SListDataValue;
import org.bimserver.interfaces.objects.SReferenceDataValue;
import org.bimserver.interfaces.objects.SSimpleDataValue;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;


public class PropertyObject 
{
	private SDataObject object;
	private ObjectContainer container;
	
	
	

	public PropertyObject(SDataObject object, ObjectContainer storey) throws ServerException, UserException
	{
		this.object = object;
		this.container = storey;
		
		getProperties();
	}

	private void getProperties() throws ServerException, UserException 
	{
		List<SDataValue> values = object.getValues();
   	 
	   	for (SDataValue value : values)
	   	{
	   		if (value.getFieldName().equals("IsDefinedBy"))
	   		{
	   			if (value instanceof SListDataValue) 
				 { 
					 SListDataValue listValue = (SListDataValue)value;
					 List<SDataValue> valueList = listValue.getValues();
					 
					 for (SDataValue data : valueList)
					 {
						if (data instanceof SReferenceDataValue)
							 {
								 String propertySetRelGuid = ((SReferenceDataValue)data).getGuid();
								 SDataObject propertySetRel = container.getObject(propertySetRelGuid);
								 
								 //System.out.println(propertySetRel.getType() + " " + propertySetRel.getName());
								 List<SDataValue> relProperties = propertySetRel.getValues();
								 
								 for (SDataValue propertyrel : relProperties)
								 {
									// System.out.println(property.getFieldName());
									 if (propertyrel.getFieldName().equals("RelatingPropertyDefinition"))
									 {
										 if (propertyrel instanceof SReferenceDataValue)
										 {
											 String propertySetGuid = ((SReferenceDataValue)propertyrel).getGuid();
											 SDataObject propertySet = container.getObject(propertySetGuid);
											 
											 List<SDataValue> propertySetList = propertySet.getValues();
											 
											 for (SDataValue propertyList : propertySetList)
											 {
												 if (propertyList.getFieldName().equals("HasProperties"))
												 {
													 if (propertyList instanceof SListDataValue)
													 {
														 List<SDataValue> propertyListValue = ((SListDataValue) propertyList).getValues();
														 
														 for (SDataValue property : propertyListValue)
														 {
															 if (property instanceof SReferenceDataValue)
															 {
																 Long propertyObjectId = ((SReferenceDataValue)property).getOid();
																 SDataObject propertySingle = 
																		 	container.getService().getDataObjectByOid(
																		 			container.getRevisionId(), 
																		 			propertyObjectId,
																		 			"IfcPropertySingleValue");
															 
																 List<SDataValue> fields = propertySingle.getValues();
																 String name = null;
																 String val = null;
																 
																 for (SDataValue field : fields)
																 {
																	 if (field instanceof SSimpleDataValue)
																	 {
																		SSimpleDataValue listv = (SSimpleDataValue)field;
																		if (listv.getFieldName().equals("Name"))
																			name = listv.getStringValue();
																		else if(listv.getFieldName().equals("NominalValue"))
																			val = listv.getStringValue();
																	 }
																 }
																 if (name != null && val != null)
																	 dimensions.put(name, val);
															 }
															 
														 }
													 }
												 }
											 }
										 }
									 }
								 }
							 }
					 }			 
				}
	   		}
	   	}
	}
	
	public void printProperties()
	{
		System.out.println();
		System.out.println();
		System.out.println("Properies for object");
		System.out.println("GUID: " + object.getGuid() 
				+ "  Type: " + object.getType()
				+ "  Name: " + object.getName());
		System.out.println();
		
		Iterator<String> it = dimensions.keySet().iterator();
		while(it.hasNext())
		{
			String propertyName = it.next();
			System.out.println("Property: " + propertyName 
					+ "  Value: " + dimensions.get(propertyName));
		}
	}
	
	public void exportAllProperties(ExcelSheet sheet) throws RowsExceededException, WriteException 
	{
		Iterator<String> it = dimensions.keySet().iterator();
		int row = 0;
		while(it.hasNext())
		{
			row++;
			exportPropertyToExcel(1, row,  it.next(), true, sheet);
		}
	}
	
	
	
	public void exportPropertyToExcel( int column, int row, String propertyName, boolean label, ExcelSheet sheet) throws RowsExceededException, WriteException 
	{
		
		if (label == true)
		{
			Label labelCell = new Label(column, row, propertyName);
			sheet.getSheet().addCell(labelCell);
			column++;
		}
		
		String valueStr = dimensions.get(propertyName);
		
		try
		{
			Double value = Double.parseDouble(valueStr);
			Number number = new Number(column, row, value.doubleValue());
			sheet.getSheet().addCell(number);
		}
		catch (NumberFormatException ex)
		{
			// not a number add the verbatim string ...
			Label valueCell = new Label(column, row, valueStr);
			sheet.getSheet().addCell(valueCell);
		}
	}
	
	public String getQuantity(String quantityName)
	{
		if (dimensions.containsKey(quantityName))
			return dimensions.get(quantityName);
		else
			return null;
	}
	
	HashMap<String, String> dimensions = new HashMap<String, String>(); 
	
	
	public static void main(String[] args) throws ServerException, UserException, RowsExceededException, BiffException, WriteException, IOException
	{
		QueryMain m = new QueryMain();
		m.connectService();
		HashSet<ObjectContainer> storeys = m.getStoreysFromServer("Tubantia", null);
		
		ExcelSheet sheet = new ExcelSheet("C:\\Users\\HartmannT\\IGPM\\Project\\Quantities.xls", 
				"C:\\Users\\HartmannT\\IGPM\\Project\\Quantities_new.xls",
				"Quantities");
		
		Iterator<ObjectContainer> it = storeys.iterator();
		while (it.hasNext())
		{
			ObjectContainer s = it.next();
			List<SDataObject> walls = s.getObjectsByType("IfcWallStandardCase");
			
			Iterator<SDataObject> wallit = walls.iterator();
			
			
			while (wallit.hasNext())
			{
				SDataObject wall = wallit.next();
				PropertyObject qo = new PropertyObject(wall, s);
				qo.printProperties();
				qo.exportAllProperties(sheet);	
			}
		}
		
		sheet.writeAndClose();
	}
}
