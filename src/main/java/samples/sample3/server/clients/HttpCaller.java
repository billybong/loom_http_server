package samples.sample3.server.clients;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@Singleton
public class HttpCaller {
    private final ObjectMapper om = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .executor((r) -> FiberScope.background().schedule(r))
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public <T> Optional<T> get(URI uri, Class<T> clazz){
        try {
            var response = httpClient.send(HttpRequest.newBuilder(uri).build(), HttpResponse.BodyHandlers.ofInputStream());
            return response.statusCode() == 404 ?
                    Optional.empty() :
                    Optional.of(om.readValue(response.body(), clazz));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
