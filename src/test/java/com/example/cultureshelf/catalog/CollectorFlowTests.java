package com.example.cultureshelf.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.example.cultureshelf.catalog.model.ImageKind;
import com.example.cultureshelf.catalog.model.MediaKind;
import com.example.cultureshelf.catalog.model.VariantType;
import com.example.cultureshelf.catalog.service.SeriesService;
import com.example.cultureshelf.catalog.service.VariantService;
import com.example.cultureshelf.catalog.web.CatalogDtos.ImageUrlRequest;
import com.example.cultureshelf.catalog.web.CatalogDtos.SearchResultDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.SeriesDetailDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.SeriesSummaryDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.VariantDto;
import com.example.cultureshelf.catalog.web.CatalogDtos.VariantRequest;
import com.example.cultureshelf.catalog.web.CatalogDtos.VolumeDto;
import com.example.cultureshelf.collection.CollectionService;
import com.example.cultureshelf.collection.CollectionService.CollectionItemRequest;
import com.example.cultureshelf.collection.CollectionStatus;
import com.example.cultureshelf.collection.ItemCondition;
import com.sun.net.httpserver.HttpServer;

/** Parcours complet contre un faux serveur MangaDex. */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:flow;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
        "cultureshelf.storage.path=target/test-uploads"
})
class CollectorFlowTests {

    private static final String MANGA = """
            {"id":"m1","type":"manga","attributes":{
              "title":{"ja-ro":"Sousou no Frieren"},
              "altTitles":[{"ja":"葬送のフリーレン"},{"fr":"Frieren"}],
              "description":[],
              "originalLanguage":"ja","year":2020,"status":"ongoing","tags":[]},
             "relationships":[
              {"id":"a1","type":"author","attributes":{"name":"Yamada Kanehito"}},
              {"id":"a2","type":"artist","attributes":{"name":"Abe Tsukasa"}},
              {"id":"c1","type":"cover_art","attributes":{"fileName":"main.jpg","volume":"1"}}]}
            """;

    private static final Map<String, String> RESPONSES = Map.of(
            "/manga", "{\"result\":\"ok\",\"data\":[" + MANGA + "],\"total\":1,\"limit\":20,\"offset\":0}",
            "/manga/m1", "{\"result\":\"ok\",\"response\":\"entity\",\"data\":" + MANGA + "}",
            "/cover", """
                    {"result":"ok","data":[
                      {"id":"cv1","type":"cover_art","attributes":{"volume":"1","fileName":"v1-ja.jpg","locale":"ja"}},
                      {"id":"cv2","type":"cover_art","attributes":{"volume":"1","fileName":"v1-fr.jpg","locale":"fr"}},
                      {"id":"cv3","type":"cover_art","attributes":{"volume":"2","fileName":"v2-ja.jpg","locale":"ja"}},
                      {"id":"cv4","type":"cover_art","attributes":{"volume":null,"fileName":"x.jpg","locale":"ja"}}
                    ],"total":4}""",
            "/manga/m1/aggregate", """
                    {"result":"ok","volumes":{"none":{"volume":"none"},"3":{"volume":"3"},"2":{"volume":"2"}}}""");

    private static final HttpServer SERVER = startServer();

    @Autowired
    SeriesService seriesService;
    @Autowired
    VariantService variantService;
    @Autowired
    CollectionService collectionService;

    @DynamicPropertySource
    static void mangaDex(DynamicPropertyRegistry registry) {
        registry.add("cultureshelf.providers.mangadex.base-url",
                () -> "http://localhost:" + SERVER.getAddress().getPort());
    }

    @AfterAll
    static void stop() {
        SERVER.stop(0);
    }

    @Test
    void searchImportAndCollect() {
        List<SearchResultDto> results = seriesService.search("MANGADEX", "frieren & co", 20);
        assertThat(results).singleElement().satisfies(r -> {
            assertThat(r.title()).isEqualTo("Frieren");
            assertThat(r.originalTitle()).isEqualTo("葬送のフリーレン");
            assertThat(r.authors()).containsExactly("Yamada Kanehito", "Abe Tsukasa");
            assertThat(r.description()).isNull();
            assertThat(r.coverUrl()).endsWith("/covers/m1/main.jpg.512.jpg");
            assertThat(r.importedSeriesId()).isNull();
        });

        SeriesDetailDto series = seriesService.importSeries("MANGADEX", "m1");
        assertThat(series.mediaKind()).isEqualTo(MediaKind.MANGA);
        assertThat(series.volumes()).extracting(VolumeDto::number).containsExactly("1", "2", "3");
        assertThat(series.volumes().getFirst().coverUrl()).endsWith("/m1/v1-fr.jpg.512.jpg");
        assertThat(series.volumes().get(2).coverUrl()).isNull();
        assertThat(series.volumes()).allSatisfy(v -> assertThat(v.variants())
                .singleElement().extracting(VariantDto::type).isEqualTo(VariantType.STANDARD));

        // Donnees collector : jaquette alternative du tome 1 avec son visuel
        VolumeDto tome1 = series.volumes().getFirst();
        VariantDto alt = variantService.create(tome1.id(), new VariantRequest(VariantType.ALTERNATE_COVER,
                "Jaquette Japan Expo", "Ki-oon", "9791032712345", null, null, 1500, null, "Ex-libris"));
        alt = variantService.addImageUrl(alt.id(), new ImageUrlRequest("https://example.com/alt.jpg", ImageKind.FRONT, null));
        assertThat(alt.images()).singleElement().extracting(i -> i.url()).isEqualTo("https://example.com/alt.jpg");

        collectionService.upsert(alt.id(), new CollectionItemRequest(CollectionStatus.OWNED, ItemCondition.MINT, 1, null, null, null));
        collectionService.upsert(tome1.variants().getFirst().id(),
                new CollectionItemRequest(CollectionStatus.WISHLIST, null, null, null, null, null));

        // Re-import idempotent : pas de doublon de tomes, les donnees collector sont conservees
        SeriesDetailDto reimported = seriesService.importSeries("MANGADEX", "m1");
        assertThat(reimported.id()).isEqualTo(series.id());
        assertThat(reimported.volumes()).hasSize(3);
        assertThat(reimported.volumes().getFirst().variants()).hasSize(2)
                .anySatisfy(v -> assertThat(v.collection().status()).isEqualTo(CollectionStatus.OWNED));

        assertThat(seriesService.search("MANGADEX", "frieren", 20).getFirst().importedSeriesId()).isEqualTo(series.id());

        SeriesSummaryDto summary = seriesService.list().getFirst();
        assertThat(summary.volumeCount()).isEqualTo(3);
        assertThat(summary.ownedVolumes()).isEqualTo(1);
        assertThat(summary.wishlistVariants()).isEqualTo(1);

        assertThat(collectionService.list(CollectionStatus.OWNED)).singleElement().satisfies(e -> {
            assertThat(e.variantName()).isEqualTo("Jaquette Japan Expo");
            assertThat(e.coverUrl()).isEqualTo("https://example.com/alt.jpg");
        });

        seriesService.delete(series.id());
        assertThat(seriesService.list()).isEmpty();
        assertThat(collectionService.list(null)).isEmpty();
    }

    private static HttpServer startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext("/", exchange -> {
                String body = RESPONSES.get(exchange.getRequestURI().getPath());
                // MangaDex refuse les requetes sans User-Agent
                boolean hasUserAgent = exchange.getRequestHeaders().getFirst("User-Agent") != null
                        && exchange.getRequestHeaders().getFirst("User-Agent").startsWith("CultureShelf");
                byte[] bytes = (body == null ? "{}" : body).getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(body != null && hasUserAgent ? 200 : 404, bytes.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                }
            });
            server.start();
            return server;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
