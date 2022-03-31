package cope.nebula.util.internal.net;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.InputStream;
import java.net.URI;

/**
 * Does things network related
 *
 * Can send/fake receive packets and can make API requests and retrieve a response
 *
 * @author aesthetical
 * @since 3/31/22
 */
public class NetworkUtil {
    /**
     * Makes a request
     * @param url the url to request to
     * @return the type or null
     */
    public static String makeRequest(String url) {
        HttpClient client = HttpClients.createDefault();

        try {
            HttpGet http = new HttpGet();

            http.setURI(new URI(url));
            http.setHeader("Content-Type", "application/json");

            HttpResponse response = client.execute(http);

            if (response.getEntity() != null && response.getEntity().getContent() != null) {
                InputStream stream = response.getEntity().getContent();

                StringBuilder text = new StringBuilder();

                int i;
                while ((i = stream.read()) != -1) {
                    text.append((char) i);
                }

                return text.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
