package me.reherhold.edifice;

import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;

import java.net.URI;

/**
 * Class that contains all the configurable options for the plugin
 */
public class EdificeConfiguration {

    public static final ObjectMapper<EdificeConfiguration> MAPPER;

    static {
        try {
            MAPPER = ObjectMapper.forClass(EdificeConfiguration.class);
        } catch (ObjectMappingException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /********************
     * General Settings *
     ********************/

    @Setting(value = "rest-uri", comment = "REST API URI") private URI restURI = URI.create("https://api.edificemc.com");
    @Setting(value = "web-uri", comment = "Website URI") private URI webURI = URI.create("edificemc.com/#"); // No 'https://www' for better UI 

    public URI getRestURI() {
        return this.restURI;
    }

    public URI getWebURI() {
        return this.webURI;
    }

}
