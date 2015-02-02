package org.visico.igpm;



import java.util.List;
import org.bimserver.client.BimServerClient;
import org.bimserver.client.ClientIfcModel;
import org.bimserver.client.json.JsonBimServerClientFactory;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.plugins.services.BimServerClientException;
import org.bimserver.shared.ChannelConnectionException;
import org.bimserver.shared.PublicInterfaceNotFoundException;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.ServiceException;
import org.bimserver.shared.exceptions.UserException;






public class QueryMain {
	private static String name = "bim.tst.user2015@gmail.com";
	private static String password = "letstestbim";
	private static String server = "http://130.89.77.159:8080/bimserver/";
	
	BimServerClient client = null;
	private List<SProject> projects = null;
	
	
	public SProject getProject(String name) {
		for (SProject project : projects) {
            if (name.equals(project.getName()))
            	return project;
		}
		System.out.println("No projects available");
		return null;
	}
	
	public ClientIfcModel getModel(String projectName) throws UserException, ServerException, BimServerClientException, PublicInterfaceNotFoundException
	{
		SProject p = getProject(projectName);
		System.out.println(p.getRevisions().toString());
		return client.getModel(p.getOid(), p.getRevisions().get(0), true);
		
	}

	public QueryMain()
	{
		
		JsonBimServerClientFactory factory = new JsonBimServerClientFactory(server);
        try {
        		client = factory.create(new UsernamePasswordAuthenticationInfo(name, password));
               
                projects = client.getBimsie1ServiceInterface().getAllProjects(true, true);
                for (SProject project : projects) {
                        System.out.println(project.getName());
                }
        } catch (ServiceException e) {
                e.printStackTrace();
        } catch (PublicInterfaceNotFoundException e) {
                e.printStackTrace();
        } catch (ChannelConnectionException e) {
                e.printStackTrace();
        }
	}

	public static void main(String[] args) {
		QueryMain m = new QueryMain();
		SProject p = m.getProject("Test");
		System.out.println(p.getName());
	}
	
}
