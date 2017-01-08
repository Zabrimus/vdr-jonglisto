package vdr.jonglisto.lib.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import vdr.jonglisto.lib.exception.Http404Exception;
import vdr.jonglisto.lib.exception.NetworkException;
import vdr.jonglisto.lib.internal.Configuration;
import vdr.jonglisto.lib.model.VDR;

public class ServiceBase {

    protected Configuration configuration = Configuration.getInstance();

    Logger log = LoggerFactory.getLogger(ServiceBase.class);

    protected ObjectMapper mapper;

    protected <T> Optional<List<T>> getJsonList(String vdrUuid, String urlPart, String arrayName, String key, Class<T> clazz) {
        try {
            JSONArray array = getJsonData(vdrUuid, urlPart).getJSONArray(arrayName);

            if (key != null) {
                return Optional.of(convertJSONArrayToList(array, key, clazz));
            } else {
                return Optional.of(convertJSONArrayToList(array, clazz));
            }
        } catch (Exception e) {
            log.error("Fehler in getJsonList1: ", e);
            return Optional.empty();
        }
    }

    protected <T> Optional<List<T>> getJsonList(String vdrUuid, String urlPart, String arrayName, Class<T> clazz) {
        try {
            JSONArray array = getJsonData(vdrUuid, urlPart).getJSONArray(arrayName);
            return Optional.of(convertJSONArrayToList(array, clazz));
        } catch (Exception e) {
            log.error("Fehler in getJsonList2: ", e);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> convertJSONArrayToList(JSONArray array, Class<T> clazz) {
        List<T> result = new ArrayList<>();

        array.forEach(s -> {
            try {
                if (clazz == String.class) {
                    result.add((T) s);
                } else {
                    result.add(mapper.readValue(s.toString(), clazz));
                }
            } catch (Exception e) {
                throw new NetworkException("unknown error; " + e.getMessage());
            }
        });

        return result;
    }

    protected <T> List<T> convertJSONArrayToList(JSONArray array, String key, Class<T> clazz) {
        List<T> result = new ArrayList<>();

        array.forEach(s -> {
            try {
                result.add(mapper.readValue(s.toString(), clazz));
            } catch (Exception e) {
                throw new NetworkException("unknown error; " + e.getMessage());
            }
        });

        return result;
    }

    private String getVdrRestUrl(String vdrUuid) {
        VDR v = configuration.getVdr(vdrUuid);
        return "http://" + v.getIp() + ":" + v.getRestfulApiPort() + "/";
    }

    protected JSONObject getJsonData(String vdrUuid, String path) {
        JSONObject result;
        HttpResponse<String> jsonResponse;

        try {
            String restUrl = getVdrRestUrl(vdrUuid) + path;

            if (log.isDebugEnabled()) {
                log.debug("GET: " + restUrl);
            }

            jsonResponse = Unirest.get(restUrl).asString();
        } catch (UnirestException e) {
            throw new NetworkException(e);
        }

        if ((jsonResponse != null) && (jsonResponse.getStatus() == 200)) {
            result = new JSONObject(jsonResponse.getBody());
        } else {
            if (jsonResponse == null) {
                throw new NetworkException("unknown error: Keine Antwort erhalten");
            } else {
                if (jsonResponse.getStatus() == 404) {
                    throw new Http404Exception("unknown error, jsonResponse: " + jsonResponse.getHeaders() + ", " + jsonResponse.getBody() + ", Code: "
                            + jsonResponse.getStatus());
                } else {
                    throw new NetworkException("unknown error, jsonResponse: " + jsonResponse.getHeaders() + ", " + jsonResponse.getBody() + ", Code: "
                            + jsonResponse.getStatus());
                }
            }
        }

        return result;
    }

    protected String put(String vdrUuid, String path, String body) {
        try {
            String restUrl = getVdrRestUrl(vdrUuid) + path;

            if (log.isDebugEnabled()) {
                log.debug("PUT: " + restUrl + "\n" + body);
            }

            HttpResponse<String> result = Unirest.put(restUrl).body(body).asString();
            if (result.getStatus() != 200) {
                throw new NetworkException("Put failed with code " + result.getStatus() + ", " + result.getStatusText());
            }

            return result.getBody();
        } catch (UnirestException e) {
            throw new NetworkException(e);
        }
    }

    protected String delete(String vdrUuid, String path, String body) {
        try {
            String restUrl = getVdrRestUrl(vdrUuid) + path;

            HttpResponse<String> result;

            if (log.isDebugEnabled()) {
                log.debug("DELETE: " + restUrl + "\n" + body);
            }

            if (body != null) {
                result = Unirest.delete(restUrl).body(body).asString();
            } else {
                result = Unirest.delete(restUrl).asString();
            }

            if (result.getStatus() == 404) {
                // recording not found, but we want to delete this
                return result.getBody();
            } else if (result.getStatus() != 200) {
                throw new NetworkException("Delete failed with code " + result.getStatus() + ", " + result.getStatusText());
            }

            return result.getBody();
        } catch (UnirestException e) {
            throw new NetworkException(e);
        }
    }

    protected String post(String vdrUuid, String path, String body) {
        String restUrl = getVdrRestUrl(vdrUuid) + path;

        try {
            HttpResponse<String> result;

            if (log.isDebugEnabled()) {
                log.debug("POST: " + restUrl + "\n" + body);
            }

            if (body != null) {
                result = Unirest.post(restUrl).body(body).asString();
            } else {
                result = Unirest.post(restUrl).asString();
            }

            if (result.getStatus() != 200) {
                throw new NetworkException("Post failed with code " + result.getStatus() + ", " + result.getStatusText());
            }

            return result.getBody();
        } catch (UnirestException e) {
            throw new NetworkException(e);
        }
    }

    protected <T> List<T> postAndGetList(String vdrUuid, String urlPart, String body, String name, Class<T> clazz) {
        JSONArray array = new JSONObject(post(vdrUuid, urlPart, body)).getJSONArray(name);
        return convertJSONArrayToList(array, clazz);
    }
}
