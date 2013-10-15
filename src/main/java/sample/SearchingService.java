package sample;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;

/**
 * A simple service that can increment a number.
 */
@Named("SearchingService")
public class SearchingService {
    String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
    String charset = "UTF-8";


    public String search(String q) throws IOException {

        HttpClient client = new HttpClient();

        GetMethod method = new GetMethod(google + URLEncoder.encode(q, charset));

        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("Search failed");
        }

        // Read the response body.
        byte[] responseBody = method.getResponseBody();


        return new String(responseBody, charset);


    }
}
