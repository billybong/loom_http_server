package samples.sample3.server.clients;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MusicBrainz {

    public String name;
    public List<Relation> relations;
    @JsonProperty("release-groups")
    public List<ReleaseGroup> releaseGroups;

    public List<ReleaseGroup> getAlbums() {
        return releaseGroups.stream()
                .filter(it -> it.primaryType.equalsIgnoreCase("album"))
                .collect(Collectors.toList());
    }

    public URI getWikipediaLink() {
        return relations.stream()
                .filter(it -> it.type.equalsIgnoreCase("wikipedia"))
                .map(it -> it.url.resource)
                .findFirst()
                .orElse(URI.create(String.format("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=true&redirects=true&titles=%s_(band)", name)));
    }

    public static class ReleaseGroup {
        public UUID id;
        @JsonProperty("primary-type")
        public String primaryType;
        public String title;
    }

    public static class Relation {
        public String type;
        public RelationURL url;
    }

    public static class RelationURL {
        public URI resource;
    }
}
