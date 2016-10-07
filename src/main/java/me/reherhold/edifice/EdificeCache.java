package me.reherhold.edifice;

import com.github.benmanes.caffeine.cache.AsyncCacheLoader;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.world.schematic.Schematic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

public class EdificeCache {

    public static AsyncLoadingCache<String, Optional<JSONObject>> createStructureCache(Path structureDirectory) {
        // TODO FIX THE EXPIRATION - ONLY FOR TESTING
        return Caffeine.newBuilder().maximumSize(100).expireAfterWrite(1, TimeUnit.MILLISECONDS)
                .buildAsync(new AsyncCacheLoader<String, Optional<JSONObject>>() {

                    @Override
                    public CompletableFuture<Optional<JSONObject>> asyncLoad(String structureId, Executor executor) {
                        return CompletableFuture.supplyAsync(() -> {
                            HttpResponse<JsonNode> response;
                            try {
                                response = Unirest.get(Edifice.config.getRestURI().toString() + "/structures/" + structureId)
                                        .asJson();
                            } catch (UnirestException e1) {
                                e1.printStackTrace();
                                return Optional.empty();
                            }

                            if (response.getStatus() != 200) {
                                return Optional.empty();
                            }
                            return Optional.of(response.getBody().getObject());
                        });
                    }
                });
    }

    public static AsyncLoadingCache<String, Optional<Schematic>> createSchematicCache(Path structureDirectory) {
        // TODO FIX THE EXPIRATION - ONLY FOR TESTING
        return Caffeine.newBuilder().maximumSize(100).expireAfterWrite(1, TimeUnit.MILLISECONDS)
                .buildAsync(new AsyncCacheLoader<String, Optional<Schematic>>() {

                    @Override
                    public CompletableFuture<Optional<Schematic>> asyncLoad(String structureId, Executor executor) {
                        final String schematicURL = "https://storage.googleapis.com/edifice-structures/" + structureId + ".schem";
                        return CompletableFuture.supplyAsync(() -> {
                            try {
                                HttpResponse<InputStream> response = Unirest.get(schematicURL)
                                        .asBinary();
                                GZIPInputStream gzipStream = new GZIPInputStream(response.getBody());
                                DataContainer schematicData = DataFormats.NBT.readFrom(gzipStream);
                                return Optional.of(DataTranslators.SCHEMATIC.translate(schematicData));
                            } catch (UnirestException | IOException e1) {
                                e1.printStackTrace();
                                return Optional.empty();
                            }
                        });
                    }
                });
    }

}
