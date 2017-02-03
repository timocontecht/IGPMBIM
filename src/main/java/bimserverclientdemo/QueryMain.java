package bimserverclientdemo;

import java.util.List;
import org.bimserver.client.BimServerClient;
import org.bimserver.client.json.JsonBimServerClientFactory;
import org.bimserver.shared.ChannelConnectionException;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.BimServerClientException;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServiceException;
import org.bimserver.client.ClientIfcModel;
import org.bimserver.interfaces.objects.SProject;
import org.bimserver.shared.exceptions.ServerException;
import org.bimserver.shared.exceptions.UserException;

 
public class QueryMain {
	private static String name = "l.l.oldescholtenhuis@utwente.nl";
	private static String password = "VISICO4D";
	private static String server = "http://bim.utwente.nl:8080";
	
	//comment out
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
		return client.getModel(p, p.getRevisions().get(p.getRevisions().size() - 1), false, true);
		
	}

	public QueryMain()
	{
		JsonBimServerClientFactory factory;
		try {
			factory = new JsonBimServerClientFactory(server);

        try {
        		client = factory.create(new UsernamePasswordAuthenticationInfo(name, password));
        		projects = client.getServiceInterface().getAllProjects(true, true);
        		//projects = client.getBimsie1ServiceInterface().getAllProjects(true, true);
               
        } catch (ServiceException e) {
                e.printStackTrace();
        } catch (PublicInterfaceNotFoundException e) {
                e.printStackTrace();
        } catch (ChannelConnectionException e) {
                e.printStackTrace();
        }
		} catch (BimServerClientException e1) {
				e1.printStackTrace();
		}
			}


	public static void main(String[] args) {
		QueryMain m = new QueryMain();
		SProject p = m.getProject("Test3");
		System.out.println(p.getName());
	}
	
}
