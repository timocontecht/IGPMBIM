package org.visico.igpm;

import java.util.ArrayList;
import java.util.List;

import org.bimserver.interfaces.objects.SDataObject;
import org.bimserver.interfaces.objects.SDataValue;
import org.bimserver.interfaces.objects.SListDataValue;
import org.bimserver.interfaces.objects.SReferenceDataValue;
import org.bimserver.shared.ServiceInterface;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;

public class SpatialStructureObject 
{
	private SDataObject spatialObject;
	private ArrayList<SDataObject> containedStructures = new ArrayList<SDataObject>();
	private ServiceInterface service;
	private Long revisionId;
	
	public SpatialStructureObject(SDataObject spatialObject, ServiceInterface service, Long revisionId) throws ServerException, UserException
	{
		this.spatialObject = spatialObject;
		this.service = service;
		this.revisionId = revisionId;
		getStructures();
	}
	
	public void getStructures() throws ServerException, UserException
	{
		List<SDataValue> values = spatialObject.getValues();
		 
		 for (SDataValue v : values)
		 {
			 if (v.getFieldName().equals("ContainsElements"))
			 {
				 if (v instanceof SListDataValue) 
				 { 
					 SListDataValue listValue = (SListDataValue)v;
					 List<SDataValue> valueList = listValue.getValues();
					 
					 for (SDataValue data : valueList)
					 {
						 if (data instanceof SReferenceDataValue)
						 {
							 String guid = ((SReferenceDataValue)data).getGuid();
							 SDataObject relContained = service.getDataObjectByGuid(revisionId, guid);
							 
							 List<SDataValue> relValues = relContained.getValues();
							 for (SDataValue r : relValues)
							 {
								 
								 if (r.getFieldName().equals("RelatedElements"))
								 {
									 if (r instanceof SListDataValue) 
									 { 
										 SListDataValue relElements = (SListDataValue)r;
										 List<SDataValue> relElementsList = relElements.getValues();
										 
										 for (SDataValue relElement : relElementsList)
										 {
											 if (relElement instanceof SReferenceDataValue)
											 {
												 guid = ((SReferenceDataValue)relElement).getGuid();
												 if (guid != null)
												 {
													 SDataObject referencedObject = service.getDataObjectByGuid(revisionId, guid);
													 containedStructures.add(referencedObject);
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

	public ArrayList<SDataObject> getContainedStructures()
	{
		return containedStructures;
	}
	
	public ArrayList<SDataObject> getContainedStructuresByType(String type)
	{
		ArrayList<SDataObject> objs = new ArrayList<SDataObject>();
		for (SDataObject o : containedStructures)
		{
			if (o.getType().equals(type))
				objs.add(o);
		}
		return objs;
		
	}
	
}
