package org.visico.igpm;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.ConnectionException;
import org.bimserver.client.factories.UsernamePasswordAuthenticationInfo;
import org.bimserver.interfaces.objects.SDataObject;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.plugins.PluginManager;
import org.bimserver.shared.ServiceInterface;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;




public class QueryMain {

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

	
	List<String> boguis = new ArrayList<String>();
	List<SDataObject> objectsPerLevel = new ArrayList<SDataObject>();
	
	long roid = 0;
	ServiceInterface service; 

}
