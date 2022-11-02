package dk.via.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
public class BranchServer {
    @Bean
    @Scope("singleton")
    public static Branch createBranch() throws URISyntaxException {
        final URI uri = new URI("http://localhost:8080/");
        HqClient hqClient = new HqClient(uri);
        return new RemoteBranch(1234, hqClient);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(BranchServer.class, args);
    }
}
