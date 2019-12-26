package samples.sample3.server.clients;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;
import java.net.URI;
import java.util.UUID;

@Singleton
public class MusicBrainzClient {
    private static final String MUSIC_BRAINZ_URL = "http://musicbrainz.org/ws/2/artist/%s?&fmt=json&inc=url-rels+release-groups";
    private final HttpCaller httpCaller;

    @Inject
    public MusicBrainzClient(HttpCaller httpCaller) {
        this.httpCaller = httpCaller;
    }

    public MusicBrainz fetchMusicBrainzInfo(UUID uuid) {
        var uri = URI.create(String.format(MUSIC_BRAINZ_URL, uuid));
        return httpCaller.get(uri, MusicBrainz.class).orElseThrow(() -> new NotFoundException("no musicbrainz info found"));
    }
}
