package uk.gov.paas.push;

import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;

import java.io.File;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {

        if(args.length != 4) {
            System.out.println("Push - minimal equivalent of a 'cf push'. Takes username/password from environment variables.");
            System.out.println("Usage: java -jar push-1.0-SNAPSHOT.jar apihost organisation space appname");
            System.exit(1);
        }

        String apiHost = args[0];
        String username = System.getenv("CF_USER");

        if(username == null) {
            System.out.println("You must set the CF_USER environment variable to a username");
            System.exit(1);
        }

        String password = System.getenv("CF_PASS");

        if(password == null) {
            System.out.println("You must set the CF_PASS environment variable to the relevant password");
            System.exit(1);
        }

        DefaultConnectionContext connectionContext = DefaultConnectionContext.builder()
                .apiHost(apiHost)
                .build();

        PasswordGrantTokenProvider tokenProvider = PasswordGrantTokenProvider.builder()
                .password(password)
                .username(username)
                .build();

        ReactorCloudFoundryClient cloudFoundryClient = ReactorCloudFoundryClient.builder()
                .connectionContext(connectionContext)
                .tokenProvider(tokenProvider)
                .build();

        DefaultCloudFoundryOperations cloudFoundryOperations = DefaultCloudFoundryOperations.builder()
                .cloudFoundryClient(cloudFoundryClient)
                .organization(args[1])
                .space(args[2])
                .build();

        Path path = new File(".").toPath();

        PushApplicationRequest pushApplicationRequest = PushApplicationRequest.builder()
                .application(path)
                .name(args[3]).build();

        cloudFoundryOperations.applications().push(pushApplicationRequest).log().block();
    }
}
