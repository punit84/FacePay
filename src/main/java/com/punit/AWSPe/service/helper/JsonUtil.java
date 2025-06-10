package com.punit.AWSPe.service.helper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class JsonUtil {
    final static Logger logger= LoggerFactory.getLogger(JsonUtil.class);

    /**
     * Merges two JSONObjects. Copies all key-value pairs from source to destination.
     *
     * @param finalJson The JSONObject to merge into
     * @param source      The JSONObject to merge from
     */
    public JSONObject mergeJsonObjects(JSONObject finalJson, JSONObject source, String cost) {

        try {
            for (String key : source.keySet()) {
                finalJson.put(key, source.get(key));
            }
            finalJson.put("Cost" , cost);
        }catch (Exception ex){
            logger.error("Error in merging json objects " + ex.getMessage());
        }

        logger.info("Json after merge: \n" + finalJson.toString());
        return finalJson;
    }

    public void printJsonbyMasking(String json) {

        // Print the JSON payload without the `data` field
        JSONObject requestForPrint = new JSONObject(json);
        JSONArray messages = requestForPrint.getJSONArray("messages");
        JSONObject firstMessage = messages.getJSONObject(0);
        JSONArray contentArray = firstMessage.getJSONArray("content");
        JSONObject firstContent = contentArray.getJSONObject(0);
        JSONObject source = firstContent.getJSONObject("source");

        source.remove("data"); // Remove the `data` field

        logger.info("Bedrock api request  " + requestForPrint.toString(4));
    }
}
