package samples.sample3.server.clients;

import com.fasterxml.jackson.databind.JsonNode;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;
import static samples.sample3.server.resilience.Retries.retry;

@Singleton
public class CoverArtClient {
    private final HttpCaller httpCaller;

    @Inject
    public CoverArtClient(HttpCaller httpCaller) {
        this.httpCaller = httpCaller;
    }

    private Optional<Album> fetchAlbum(MusicBrainz.ReleaseGroup album) {
        var uri = URI.create(String.format("http://coverartarchive.org/release-group/%s", album.id));
        var mayBeJson = httpCaller.get(uri, JsonNode.class);
        if (mayBeJson.isEmpty()){
            return Optional.empty();
        }
        var json = mayBeJson.get();
        var imageUri = URI.create(json.get("images").get(0).get("image").asText());
        return Optional.of(new Album(album.id, album.title, imageUri));
    }

    public List<CompletableFuture<Optional<Album>>> fetchAlbums(List<MusicBrainz.ReleaseGroup> releaseGroups) {
            return releaseGroups.stream()
                    .map(album -> CompletableFuture.supplyAsync(retry(() -> fetchAlbum(album))))
                    .collect(toList());
    }

    public static class Album {
        UUID id;
        String name;
        URI image;

        Album(UUID id, String name, URI image) {
            this.id = id;
            this.name = name;
            this.image = image;
        }
    }
}
