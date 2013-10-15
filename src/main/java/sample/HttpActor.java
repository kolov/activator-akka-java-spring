package sample;

import akka.actor.UntypedActor;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.springframework.context.annotation.Scope;

import javax.inject.Named;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.Executors;

/**
 * An actor that can count using an injected CountingService.
 *
 * @note The scope here is prototype since we want to create a new actor
 * instance for use of this bean.
 */
@Named("HttpActor")
@Scope("prototype")
class HttpActor extends UntypedActor {

    String charset = "UTF-8";

    public static class Get {


        public String getAddress() {
            return address;
        }

        public String getPath() {
            return path;
        }

        public Get(String address, String path) {

            this.address = address;
            this.path = path;
        }

        private String address;
        private String path;
    }

    public static class Response {
        private String body;

        public String getBody() {
            return body;
        }

        public Response(String body) {
            this.body = body;
        }
    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Get) {
            Get get = (Get) message;
            String result = query(get.getAddress(), get.getPath());
            getSender().tell(new Response(result), getSelf());
        } else {
            unhandled(message);
        }
    }

    private String query(String address, String path) throws IOException {
        HttpClient client = new HttpClient();

        GetMethod method = new GetMethod("http://" + address + path);

        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("Qu failed");
        }

        // Read the response body.
        byte[] responseBody = method.getResponseBody();


        return new String(responseBody, charset);
    }
}
