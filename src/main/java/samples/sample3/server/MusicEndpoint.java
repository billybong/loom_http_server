package samples.sample3.server;

import com.fasterxml.jackson.databind.JsonNode;
import samples.sample3.server.clients.HttpCaller;
import samples.sample3.server.clients.MusicBrainz;

import javax.ws.rs.*;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Path("music")
public class MusicEndpoint {

    private static final String MUSIC_BRAINZ_URL = "http://musicbrainz.org/ws/2/artist/%s?&fmt=json&inc=url-rels+release-groups";
    private static final String COVER_ART_URL = "http://coverartarchive.org/release-group/%s";
    private final HttpCaller httpCaller;

    public MusicEndpoint() {
        this.httpCaller = new HttpCaller();
    }

    public MusicEndpoint(HttpCaller httpCaller) {
        this.httpCaller = httpCaller;
    }

    @GET
    @Produces("application/json")
    @Path("/{id}")
    public Map<String, Object> musicInfo(@PathParam("id") String id) {
        var uuid = UUID.fromString("5b11f4ce-a62d-471e-81fc-a69a8278c7da");
        var musicBrainz = fetchMusicBrainzInfo();
        var albumsFibers = getAlbums(musicBrainz.getAlbums());
        var wikipediaDescription = fetchWikipediaInfo(musicBrainz.getWikipediaLink());
        var albums = albumsFibers.stream()
                .map(it -> it.toFuture().join())
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
        return Map.of("mbid", uuid,
                "description", wikipediaDescription,
                "albums", albums);
    }

    private MusicBrainz fetchMusicBrainzInfo() {
        var uri = URI.create(String.format(MUSIC_BRAINZ_URL, "5b11f4ce-a62d-471e-81fc-a69a8278c7da"));
        return httpCaller.get(uri, MusicBrainz.class).orElseThrow(() -> new NotFoundException("no musicbrainz info found"));
    }

    private List<Fiber<Optional<Album>>> getAlbums(List<MusicBrainz.ReleaseGroup> releaseGroups) {
        try (var scope = FiberScope.open()) {
            return releaseGroups.stream()
                    .map(album -> scope.schedule(retry(() -> fetchAlbum(album))))
                    .collect(Collectors.toList());
        }
    }

    private Optional<Album> fetchAlbum(MusicBrainz.ReleaseGroup album) {
        var uri = URI.create(String.format(COVER_ART_URL, album.id));
        var mayBeJson = httpCaller.get(uri, JsonNode.class);
        if (mayBeJson.isEmpty()){
            return Optional.empty();
        }
        var json = mayBeJson.get();
        var imageUri = URI.create(json.get("images").get(0).get("image").asText());
        return Optional.of(new Album(album.id, album.title, imageUri));
    }

    private String fetchWikipediaInfo(URI wikipediaLink) {
        var json = httpCaller.get(wikipediaLink, JsonNode.class).orElseThrow(() -> new NotFoundException("wikipedia details not found"));
        return json.findValues("extract").get(0).asText();
    }

    public static class Album {
        public UUID id;
        public String name;
        public URI image;

        public Album(UUID id, String name, URI image) {
            this.id = id;
            this.name = name;
            this.image = image;
        }
    }

    public static <T> Callable<T> retry(Callable<T> callable) {
        return () -> {
            int triesLef = 3;
            Exception lastException = null;
            while (triesLef > 0) {
                try {
                    return callable.call();
                } catch (Exception e) {
                    --triesLef;
                    lastException = e;
                }
            }
            throw new RuntimeException(lastException);
        };
    }
}
