package io.github.aangiel.teryt.redis;

public class RedisWriter {
}


//    void writeToRedis() throws IOException, InterruptedException {
//        var client = new JedisPooled();
//
//
//        var downloaded = download();
//
//
//        Pipeline pipelined = new Jedis(client.getPool().getResource()).pipelined();
//
//        Map<String, String> voivodeships = downloaded.get("V").stream()
//                .filter(e -> "województwo".equals(e.getExtraName()))
//                .collect(Collectors.toMap(CsvLine::getVoivodeship, CsvLine::getName));
//
//        Map<String, String> counties = downloaded.get("C").stream()
//                .filter(e -> e.getExtraName().contains("powiat"))
//                .collect(
//                        Collectors.toMap(
//                                e -> String.format("%s:%s", e.getVoivodeship(), e.getCounty()),
//                                CsvLine::getName)
//                );
//
//        Map<String, String> communities = downloaded.get("R").stream()
//                .filter(Predicate.not(e -> "województwo".equals(e.getExtraName())))
//                .filter(Predicate.not(e -> e.getExtraName().contains("powiat")))
//                .map(e -> MapEntry.create(
//                        String.format("%s:%s:%s", e.getVoivodeship(), e.getCounty(), e.getCommunity()),
//                        e.getName()
//                ))
//                .distinct()
//                .collect(
//                        Collectors.toMap(MapEntry::getKey, MapEntry::getValue)
//                );
//
//        var all = ImmutableList.<CsvLine>builder()
//                .addAll(downloaded.get("V"))
//                .addAll(downloaded.get("C"))
//                .addAll(downloaded.get("R"));
//
//        for (var v : all.build()) {
//            pipelined.hset(v.getKey(), v.getValue(voivodeships, counties, communities));
//        }
//        pipelined.sync();
//
//
//        Schema schema = new Schema()
//                .addTextField("name", 10.0)
////                .addTextField("community", 7.5)
//                .addTextField("county", 5.0)
//                .addTextField("voivodeship", 1.0);
//
//        IndexDefinition rule = new IndexDefinition(IndexDefinition.Type.HASH)
//                .setPrefixes("terc:2022-01-01:");
//
//
//        try {
//            client.ftCreate("idx:terc:2022-01-01:name", IndexOptions.defaultOptions().setDefinition(rule), schema);
//        } catch (JedisDataException e) {
//            e.printStackTrace();
//        }
//    }