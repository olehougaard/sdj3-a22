package dk.via.bank;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.net.URI;

public class BranchServer {
    public static void main(String[] args) throws Exception {
        final URI uri = new URI("http://localhost:8080/");
        HqClient hqClient = new HqClient(uri);
        Branch branch = new RemoteBranch(1234, hqClient);
        Server server = ServerBuilder
                .forPort(9090)
                .addService(new BranchServiceImpl(branch))
                .build();
        server.start();
        server.awaitTermination();
    }
}
