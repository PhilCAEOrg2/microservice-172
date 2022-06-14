package i5.las2peer.services.res;

import com.github.viclovsky.swagger.coverage.CoverageOutputWriter;
import com.github.viclovsky.swagger.coverage.FileSystemOutputWriter;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import io.swagger.models.Operation;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.PathParameter;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import static com.github.viclovsky.swagger.coverage.SwaggerCoverageConstants.BODY_PARAM_NAME;
import static com.github.viclovsky.swagger.coverage.SwaggerCoverageConstants.OUTPUT_DIRECTORY;

public class MiniClientCoverage extends MiniClient {

    private CoverageOutputWriter writer;
    private String basePath;

    public MiniClientCoverage(String basePath) {
        this.writer = new FileSystemOutputWriter(Paths.get(OUTPUT_DIRECTORY));
        this.basePath = basePath;
    }

    public ClientResponse sendRequest(String method, String uri, String content, String contentType, String accept, Map<String, String> headers, Object... pathParameters) throws Exception {
        String originalUri = new String(uri);

        long parameterCount = getUnsetParameterCount(uri);
        if(parameterCount != pathParameters.length) throw new Exception("Number of path parameters in URI differs from number of given path parameters.");

        Operation operation = new Operation();

        for(int i = 0; i < parameterCount; i++) {
            Pattern p = Pattern.compile("\\{([^}]*)}");
            MatchResult firstMatch = p.matcher(uri).results().findFirst().get();
            String paramName = uri.substring(firstMatch.start()+1, firstMatch.end()-1);
            String paramValue = pathParameters[i].toString();
            uri = p.matcher(uri).replaceFirst(paramValue);
            operation.addParameter(new PathParameter().name(paramName).example(paramValue));
        }

        if(headers != null) {
            headers.forEach((name, value) -> operation.addParameter(new HeaderParameter().name(name).example(value)));
        }

        if(content != null) {
            operation.addParameter(new BodyParameter().name(BODY_PARAM_NAME));
        }

        ClientResponse response = super.sendRequest(method, basePath + uri, content, contentType, accept, headers);

        operation.addResponse("" + response.getHttpCode(), new io.swagger.models.Response());

        Swagger swagger = new Swagger()
                .scheme(Scheme.HTTP)
                .host(URI.create(uri).getHost())
                .consumes(contentType)
                .produces(response.getHeader("Content-Type"))
                .path(originalUri, new io.swagger.models.Path().set(method.toLowerCase(), operation));

        writer.write(swagger);

        return response;
    }

    private long getUnsetParameterCount(String uri) {
        Pattern p = Pattern.compile("\\{([^}]*)}");
        return p.matcher(uri).results().count();
    }
}
