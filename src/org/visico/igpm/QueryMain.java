package org.visico.igpm;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.ConnectionException;
import org.bimserver.client.factories.UsernamePasswordAuthenticationInfo;
import org.bimserver.interfaces.objects.SDataObject;
import org.bimserver.interfaces.objects.SDataValue;
import org.bimserver.interfaces.objects.SListDataValue;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.interfaces.objects.SReferenceDataValue;
import org.bimserver.interfaces.objects.SSimpleDataValue;
import org.bimserver.plugins.PluginManager;
import org.bimserver.shared.ServiceInterface;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.ServiceException;
import org.bimserver.shared.exceptions.UserException;




public class QueryMain {
	
	public static void main(String[] args) throws ServiceException, ConnectionException
	{
		QueryMain q = new QueryMain();
		q.connectService();
		
		q.getObjectsByLevel();
		
		q.printQuantities();
		
	}

	public long getRoid() {
		return roid;
	}

	public void setRoid(long roid) {
		this.roid = roid;
	}

	public ServiceInterface getService() {
		return service;
	}

	public void setService(ServiceInterface service) {
		this.service = service;
	}

	private void printQuantities() throws ServerException, UserException 
	{
		System.out.println();
		System.out.println("quantities:");
		for (SDataObject o : objectsPerLevel)
		{
			 List<SDataValue> values = o.getValues();
        	 
        	 for (SDataValue value : values)
        	 {
        		 String fieldName = value.getFieldName();
        		 
        		 if (fieldName.equals("IsDefinedBy") )
        		 {
        			// System.out.println(value.getClass().toString());
        			 if (value instanceof SListDataValue)
        			 {
        				 SListDataValue listValue = (SListDataValue)value;
        				 List<SDataValue> valueList = listValue.getValues();
       				 
        				 for (SDataValue list : valueList)
        				 {
        					 if (list instanceof SReferenceDataValue)
        					 {
        						 String guid = ((SReferenceDataValue) list).getGuid();
        						 SDataObject referenceObject = service.getDataObjectByGuid(roid, guid);

        						 if (referenceObject.getType().equals("IfcRelDefinesByProperties") )
        						 {
        							 
        							 List<SDataValue> relProps = referenceObject.getValues();
        							 System.out.println(referenceObject.getName() + " " + referenceObject.getGuid());
        							 for (SDataValue relProp : relProps)
        							 {
        								 if (relProp.getFieldName().equals("RelatingPropertyDefinition"))
        								 {
        									 String psetGuid = ((SReferenceDataValue) relProp).getGuid();
        									 SDataObject pset = service.getDataObjectByGuid(roid, psetGuid);
        									 
        									 List<SDataValue> properties = pset.getValues();
        									 for (SDataValue property : properties)
        									 {
        										 if (property.getFieldName().equals("HasProperties"))
        										 {
        											// System.out.println(property.getFieldName() + " " + property.getClass().toString());
        											 if (property instanceof SListDataValue)
        											 {
        												// System.out.println(property.getClass().toString()	);
        												 List<SDataValue> propValues = ((SListDataValue) property).getValues();
        	        									 for (SDataValue propValue : propValues)
        	        									 {
        	        										 System.out.println(propValue.getFieldName() + " " );
        	        										 System.out.println(propValue.getClass().toString());
        	        										 SReferenceDataValue v = (SReferenceDataValue) propValue;
        	        										 
        	        										 
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

	public void connectService()
	{
		try
		{
			 BimServerClient bimServerClient;
				PluginManager pluginManager = new PluginManager();
				bimServerClient = new BimServerClient(pluginManager);
		        
		        bimServerClient.setAuthentication(new UsernamePasswordAuthenticationInfo("hartmann.utwente@gmail.com", "Powerman"));
		        bimServerClient.connectSoap("http://localhost:8082/soap", false);
		        service = bimServerClient.getServiceInterface();
		    } catch (ConnectionException e) {
		        e.printStackTrace();
			}
	}
	
	public HashSet<Storey> getStoreysFromServer(String projectName, Long revisionId) throws ServerException, UserException 
	{
		 List<SProject> projects = service.getAllProjects();
         SProject project = null;
         
         if (projects == null)
         {
         	throw new RuntimeException("No projects");
         	
         }
         if (projects.isEmpty()) {
             throw new RuntimeException("No projects");
            
         }
         for (SProject p : projects) {
             String pN = p.getName();
             
             if (pN.equals(projectName))
             {
            	 project = p;
             }
         }
 
		return getStoreysFromServer(project, revisionId);
	}
	
	private HashSet<Storey> getStoreysFromServer(SProject project, Long revisionId) throws ServerException, UserException
	{
		HashSet<Storey> storeys = new HashSet<Storey>();
		
	
		if (project == null)
			throw new RuntimeException("No project");
		
		// if the revision id is null use the latest revision
		if (revisionId == null)
			revisionId = project.getLastRevisionId();
    	
    	// get storey elements
        List<SDataObject> objects = 
        	service.getDataObjectsByType(revisionId, "IfcBuildingStorey");
        
        for (SDataObject s : objects)
        {
        	Storey storey = new Storey(s, service, revisionId);
        	storeys.add(storey);
        }
		
		return storeys;
	}
	
	private void getObjectsByLevel()  {
		
		
           
            try {
                    
                    List<SProject> projects = service.getAllProjects();
                   
                    if (projects == null)
                    {
                    	throw new RuntimeException("No projects");
                    }
                    if (projects.isEmpty()) {
                        throw new RuntimeException("No projects");
                    }
                    for (SProject project : projects) {
                        String projectName = project.getName();
                        
                        if (projectName.equals("Tubantia"))
                        {
                        	List<String> classes = service.getAvailableClasses();
                        	
                        	for (String s : classes)
                        	{
                        		System.out.println(s);
                        	}
                        	
                        	roid = project.getLastRevisionId();
                        	
                        	// get storey elements
                            List<SDataObject> objects = 
                            	service.getDataObjectsByType(roid, "IfcBuildingStorey");
                             
	                             for (SDataObject object : objects)
	                             {
	                            	 System.out.println(object.getName() + " " + object.getType());
	                            	 List<SDataValue> values = object.getValues();
	                            	 
	                            	 for (SDataValue value : values)
	                            	 {
	                            		 System.out.println(value.getFieldName());
	                            		 
	                            		 if (value.getFieldName().equals("ContainsElements"))
	                            		 {
	                            			 if ( value instanceof SListDataValue) 
	                            			 { 
	                            				 SListDataValue listValue = (SListDataValue)value;
	                            				 List<SDataValue> valueList = listValue.getValues();
	                           				 
	                            				 for (SDataValue list : valueList)
	                            				 {
	                            					 if (list instanceof SReferenceDataValue)
	                            					 {
	                            						 String guid = ((SReferenceDataValue) list).getGuid();
	                            						 System.out.println(guid);
	                            						 
	                            						 SDataObject referenceObject = service.getDataObjectByGuid(roid, guid);
	                            						 
	                            						 for (SDataValue buildingObjectGui : referenceObject.getValues())
	                            						 {
	                            							 System.out.println(buildingObjectGui.getFieldName() + " " + buildingObjectGui.getClass().toString());
	                            							 if (buildingObjectGui instanceof SListDataValue)
	                            							 {
	                            								 SListDataValue BOlistValue = (SListDataValue)buildingObjectGui;
	            	                            				 List<SDataValue> BOvalueList = BOlistValue.getValues();
	            	                           				 
	            	                            				 for (SDataValue BOlist : BOvalueList)
	            	                            				 {
	            	                            					 if (BOlist instanceof SReferenceDataValue)
	            	                            					 {
	            	                            						 String boguid = ((SReferenceDataValue) BOlist).getGuid();
	            	                            						 System.out.println(boguid);
	            	                            						 boguis.add(boguid);
	            	                            						 SDataObject buildingObject = service.getDataObjectByGuid(roid, boguid);
	            	                            						 System.out.println(buildingObject.getType());
	            	                            						 
	            	                            						 // select all walls
	            	                            						 if (buildingObject.getType().equals("IfcWallStandardCase") )
	            	                            						 {
	            	                            							 objectsPerLevel.add(buildingObject);
	            	                            						 }
	            	                            						 
	            	                            						 // TODO: select other building components
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
                        System.out.println("Number of Walls on Storey: " + objectsPerLevel.size());
                    }
            }
             catch (ServerException e) {
                    e.printStackTrace();
            } catch (UserException e) {
                    e.printStackTrace();
            }
    
	}
	
	
	List<String> boguis = new ArrayList<String>();
	List<SDataObject> objectsPerLevel = new ArrayList<SDataObject>();
	
	long roid = 0;
	ServiceInterface service; 

}
