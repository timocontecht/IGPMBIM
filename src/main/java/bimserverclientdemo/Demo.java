package bimserverclientdemo;

import org.bimserver.client.BimServerClient;
import org.bimserver.client.json.JsonBimServerClientFactory;
import org.bimserver.shared.ChannelConnectionException;
import org.bimserver.shared.UsernamePasswordAuthenticationInfo;
import org.bimserver.shared.exceptions.BimServerClientException;
import org.bimserver.shared.exceptions.PublicInterfaceNotFoundException;
import org.bimserver.shared.exceptions.ServiceException;

public class Demo {
			public static void main(String[] args) {
			try {
				//JsonBimServerClientFactory clientFactory = new JsonBimServerClientFactory("http://localhost:8080");
				JsonBimServerClientFactory clientFactory = new JsonBimServerClientFactory("http://bim.utwente.nl:8080");
				BimServerClient client = clientFactory
						//.create(new UsernamePasswordAuthenticationInfo("admin@bimserver.org", "admin"));
						.create(new UsernamePasswordAuthenticationInfo("l.l.oldescholtenhuis@utwente.nl", "password"));
				client.getServiceInterface().addProject("testOne", "ifc2x3tc1");
			} catch (BimServerClientException | ServiceException | ChannelConnectionException e) {
				e.printStackTrace();
			} catch (PublicInterfaceNotFoundException e) {
				e.printStackTrace();
			}
		}
	}


//

