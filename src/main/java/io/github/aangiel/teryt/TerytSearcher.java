package io.github.aangiel.teryt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.Getter;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import java.io.Serializable;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TerytSearcher {

    public static String search(String searchString) throws JsonProcessingException {

        SearchResult sr;
        try (JedisPooled client = new JedisPooled()) {

            Query q = new Query("%" + searchString + "%")
                    .limit(0, 10);

            sr = client.ftSearch("idx:terc:2022-01-01:name", q);
        }

        var result = ImmutableList.<ResultElement>builder();

        for (var res : sr.getDocuments()) {

            var props = StreamSupport.stream(res.getProperties().spliterator(), false)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            v -> (String) v.getValue()
                    ));

            var item = ResultElement.builder()
                    .id(res.getId())
                    .voivodeship(props.get("voivodeship"))
                    .county(props.get("county"))
                    .community(props.get("community"))
                    .extraName(props.get("extraName"))
                    .name(props.get("name"))
                    .build();

            result.add(item);
        }

        return new ObjectMapper().writeValueAsString(result.build());
    }

    @Builder
    @Getter
    static class ResultElement implements Serializable {
        private String id;
        private String voivodeship;
        private String county;
        private String community;
        private String extraName;
        private String name;
    }
}
