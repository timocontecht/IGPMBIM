package org.visico.igpm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bimserver.interfaces.objects.SDataObject;
import org.bimserver.interfaces.objects.SDataValue;
import org.bimserver.interfaces.objects.SListDataValue;
import org.bimserver.interfaces.objects.SReferenceDataValue;
import org.bimserver.shared.ServiceInterface;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;

public class Storey 
{
	public Storey (SDataObject soapStorey, ServiceInterface service, Long revisionId) throws ServerException, UserException
	{
		this.service = service;
		this.revisionId = revisionId;
		this.storey = soapStorey;
	
		getAllLinkedObjects(storey);
	}
	
	public ServiceInterface getService() {
		return service;
	}

	public void setService(ServiceInterface service) {
		this.service = service;
	}

	public Long getRevisionId() {
		return revisionId;
	}

	public void setRevisionId(Long revisionId) {
		this.revisionId = revisionId;
	}

	private void getAllLinkedObjects(SDataObject o) throws ServerException, UserException 
	{
		String guid = o.getGuid();
		if (objects.containsKey(guid) )
			return;
		
		objects.put(o.getGuid(), o);
		List<SDataValue> values = o.getValues();
		 
		 for (SDataValue v : values)
		 {
			 if (v instanceof SReferenceDataValue)
			 {
				 parseReferenceDataValue((SReferenceDataValue)v);
			 }
			 
			 if (v instanceof SListDataValue) 
			 { 
				 SListDataValue listValue = (SListDataValue)v;
				 List<SDataValue> valueList = listValue.getValues();
				 
				 for (SDataValue data : valueList)
				 {
					 if (data instanceof SReferenceDataValue)
					 {
						 parseReferenceDataValue((SReferenceDataValue)data);
					 }
				 }
			 }
		 }
	}
	
	public void parseReferenceDataValue(SReferenceDataValue v) throws ServerException, UserException
	{
		 String guid = v.getGuid();
		 if (objects.containsKey(guid) )
				return;
		 
		 if (guid != null)
		 {
			 SDataObject referencedObject = service.getDataObjectByGuid(revisionId, guid);
			 getAllLinkedObjects(referencedObject);
		 }
	}
	
	
	public void printObjectInfo()
	{
		System.out.println();
		System.out.println();
		System.out.println("Object Information for Storey");
		System.out.println("GUID: " + storey.getGuid() 
				+ "  Type: " + storey.getType()
				+ "  Name: " + storey.getName());
		System.out.println();
		Iterator<SDataObject> it = objects.values().iterator();
		while(it.hasNext())
		{
			SDataObject o = it.next();
			System.out.println("GUID: " + o.getGuid() 
					+ "  Type: " + o.getType()
					+ "  Name: " + o.getName());
		}
	}
	
	public List<SDataObject> getObjectsByType(String typeName)
	{
		ArrayList<SDataObject> os = new ArrayList<SDataObject>();
		Iterator<SDataObject> it = objects.values().iterator();
		
		while (it.hasNext())
		{
			SDataObject o = it.next();
			if (o.getType().equals(typeName))
			{
				os.add(o);
			}
		}
		
		return os;
	}
	
	public SDataObject getObject(String propertySetGuid) 
	{
		return objects.get(propertySetGuid);
	}

	HashMap<String, SDataObject> objects = new HashMap<String, SDataObject>();
	ServiceInterface service;
	Long revisionId;
	SDataObject storey;
	
	public static void main(String[] args) throws ServerException, UserException
	{
		QueryMain m = new QueryMain();
		m.connectService();
		HashSet<Storey> storeys = m.getStoreysFromServer("Tubantia", null);
		
		Iterator<Storey> it = storeys.iterator();
		while (it.hasNext())
		{
			it.next().printObjectInfo();
		}
	}

	
}
