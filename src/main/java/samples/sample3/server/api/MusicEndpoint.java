package samples.sample3.server.api;

import samples.sample3.server.clients.CoverArtClient;
import samples.sample3.server.clients.MusicBrainzClient;
import samples.sample3.server.clients.WikipediaClient;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

@Path("music")
public class MusicEndpoint {

    private final WikipediaClient wikipediaClient;
    private final MusicBrainzClient musicBrainzClient;
    private final CoverArtClient coverArtClient;

    @Inject
    public MusicEndpoint(WikipediaClient wikipediaClient, MusicBrainzClient musicBrainzClient, CoverArtClient coverArtClient) {
        this.wikipediaClient = wikipediaClient;
        this.musicBrainzClient = musicBrainzClient;
        this.coverArtClient = coverArtClient;
    }

    @GET
    @Produces("application/json")
    @Path("/{id}")
    public Map<String, Object> musicInfo(@PathParam("id") String id) {
        var uuid = UUID.fromString("5b11f4ce-a62d-471e-81fc-a69a8278c7da");
        var musicBrainzInfo = musicBrainzClient.fetchMusicBrainzInfo(uuid);
        var albumFutures = coverArtClient.fetchAlbums(musicBrainzInfo.getAlbums());
        var wikipediaDescription = wikipediaClient.fetchWikipediaInfo(musicBrainzInfo);
        var albums = awaitAll(albumFutures);
        return Map.of(
                "mbid",         uuid,
                "description",  wikipediaDescription,
                "albums",       albums
        );
    }

    private static <T> List<T> awaitAll(List<CompletableFuture<Optional<T>>> futuresOfOptionals) {
        return futuresOfOptionals.stream()
                .map(CompletableFuture::join)
                .flatMap(Optional::stream)
                .collect(toList());
    }
}
