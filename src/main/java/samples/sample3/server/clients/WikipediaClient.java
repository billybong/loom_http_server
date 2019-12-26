package samples.sample3.server.clients;

import com.fasterxml.jackson.databind.JsonNode;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.NotFoundException;

@Singleton
public class WikipediaClient {
    private final HttpCaller httpCaller;

    @Inject
    public WikipediaClient(HttpCaller httpCaller) {
        this.httpCaller = httpCaller;
    }

    public String fetchWikipediaInfo(MusicBrainz musicBrainz) {
        var json = httpCaller.get(musicBrainz.getWikipediaLink(), JsonNode.class).orElseThrow(() -> new NotFoundException("wikipedia details not found"));
        return json.findValues("extract").get(0).asText();
    }
}
