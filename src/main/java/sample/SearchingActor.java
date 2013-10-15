package sample;

import akka.actor.UntypedActor;
import org.springframework.context.annotation.Scope;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * An actor that can count using an injected CountingService.
 *
 * @note The scope here is prototype since we want to create a new actor
 * instance for use of this bean.
 */
@Named("SearchingActor")
@Scope("prototype")
class SearchingActor extends UntypedActor {

    public static class Search {
        public Search(String query) {
            this.query = query;
        }

        public String getQuery() {
            return query;
        }

        private String query;
    }


    // the service that will be automatically injected
    final SearchingService searchingService;

    @Inject
    public SearchingActor(@Named("SearchingService") SearchingService searchingService) {
        this.searchingService = searchingService;
    }

    private int count = 0;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof Search) {
            String searchResult = searchingService.search(((Search) message).getQuery());
            getSender().tell(searchResult, getSelf());
        } else {
            unhandled(message);
        }
    }
}
