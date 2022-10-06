package dk.via.bank;

import javax.xml.ws.Endpoint;
import java.net.URI;
import java.net.URL;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class RunBranch {
	public static void main(String[] args) throws Exception {
        final URI uri = new URI("http://localhost:8080/");
		HqClient hqClient = new HqClient(uri);
		RemoteBranch branch = new RemoteBranch(1234, hqClient);
		Remote exportedBranch = UnicastRemoteObject.exportObject(branch, 0);
		Registry registry = LocateRegistry.createRegistry(1099);
		registry.bind("branch", exportedBranch);
		System.out.println("Server running");
	}
}
