package me.reherhold.edifice;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class StructureCache {

    private Path structureDirectory;

    public StructureCache(Path structureDirectory) {
        this.structureDirectory = structureDirectory;
        // Make sure the directory exists
        structureDirectory.toFile().mkdirs();
    }

    public CompletableFuture<Optional<JSONObject>> getById(String structureId) {
        return CompletableFuture.supplyAsync(() -> {
            File structureFile = this.structureDirectory.resolve(structureId + ".json").toFile();

            Calendar lastModified = Calendar.getInstance();
            lastModified.setTimeInMillis(structureFile.lastModified());
            Calendar minCreationDate = Calendar.getInstance(); // Minimum date
                                                               // for a
                                                               // structure in
                                                               // the cache to
                                                               // be created in
                                                               // order to not
                                                               // be expired
            minCreationDate.add(Calendar.DAY_OF_YEAR, -1); // Cache entries
                                                           // expire in a day

            if (structureFile.exists() && lastModified.after(minCreationDate)) {
                // Read the cached structure from the file
                return readFromFile(structureFile);

            } else {
                HttpResponse<JsonNode> response;
                try {
                    response = Unirest.get(Edifice.config.getRestURI().toString() + "/structures/" + structureId)
                            .asJson();
                } catch (UnirestException e1) {
                    e1.printStackTrace();
                    return Optional.empty();
                }

                if (response.getStatus() != 200) {
                    // Check if there's a cached copy to use
                    if (structureFile.exists()) {
                        return readFromFile(structureFile);
                    } else {
                        return Optional.empty();
                    }
                }
                JSONObject structure = response.getBody().getObject();

                try {
                    FileWriter writer = new FileWriter(structureFile);
                    writer.write(structure.toString());
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return Optional.of(structure);
            }
        });
    }

    private Optional<JSONObject> readFromFile(File file) {
        try {
            String jsonTxt = new Scanner(file).useDelimiter("\\Z").next();
            return Optional.of(new JSONObject(jsonTxt));
        } catch (FileNotFoundException e) {
            // This shouldn't ever happen
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
