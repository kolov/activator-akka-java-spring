package sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;

import static akka.pattern.Patterns.ask;

import akka.util.Timeout;
import org.springframework.context.annotation.Scope;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;
import static sample.SpringExtension.SpringExtProvider;

/**
 * An actor that can count using an injected CountingService.
 *
 * @note The scope here is prototype since we want to create a new actor
 * instance for use of this bean.
 */
@Named("SearchingActor")
@Scope("prototype")
class SearchingActor extends UntypedActor {

    String address = "ajax.googleapis.com";
    String pathPrefix = "/ajax/services/search/web?v=1.0&q=";
    String charset = "UTF-8";


    @Inject
    private ActorSystem system;

    public static class Search {
        public Search(String query) {
            this.query = query;
        }

        public String getQuery() {
            return query;
        }

        private String query;
    }

    public static class SearchResult {
        public SearchResult(String result) {
            this.result = result;
        }

        public String getResult() {
            return result;
        }

        private String result;

        @Override
        public String toString() {
            return "SearchResult{" +
                    "result='" + result + '\'' +
                    '}';
        }
    }


    // the service that will be automatically injected
    final SearchingService searchingService;

    @Inject
    public SearchingActor(@Named("SearchingService") SearchingService searchingService) {
        this.searchingService = searchingService;
    }

    FiniteDuration duration = FiniteDuration.create(3, TimeUnit.SECONDS);

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Search) {

            String q = ((Search) message).getQuery();

            String path = pathPrefix + URLEncoder.encode(q, charset);

            ActorRef actorRef = system.actorOf(
                    SpringExtProvider.get(system).props("HttpActor"), "http1");

            Future<Object> result = ask(actorRef, new HttpActor.Get(address, path), Timeout.durationToTimeout(duration));

            try {
                HttpActor.Response x = (HttpActor.Response) Await.result(result, duration);
                getSender().tell(new SearchResult(x.getBody()), getSelf());
            } catch (Exception e) {
                System.err.println("Failed getting result: " + e.getMessage());
                throw e;
            }


        } else {
            unhandled(message);
        }
    }
}
