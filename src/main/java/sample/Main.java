package sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import static akka.pattern.Patterns.ask;
import akka.util.Timeout;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import sample.CountingActor.Count;
import sample.CountingActor.Get;
import static sample.SpringExtension.SpringExtProvider;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;
import java.util.concurrent.TimeUnit;

/**
 * A main class to start up the application.
 */
public class Main {
  public static void main(String[] args) throws Exception {
    // create a spring context and scan the classes
    AnnotationConfigApplicationContext ctx =
      new AnnotationConfigApplicationContext();
    ctx.scan("sample");
    ctx.refresh();

    // get hold of the actor system
    ActorSystem system = ctx.getBean(ActorSystem.class);
    // use the Spring Extension to create props for a named actor bean
    ActorRef searcher = system.actorOf(
      SpringExtProvider.get(system).props("SearchingActor"), "s1");



    // print the result
    FiniteDuration duration = FiniteDuration.create(3, TimeUnit.SECONDS);
    Future<Object> result = ask(searcher, new SearchingActor.Search("akka"), Timeout.durationToTimeout(duration));
    try {
      System.out.println("Got back " + Await.result(result, duration));
    } catch (Exception e) {
      System.err.println("Failed getting result: " + e.getMessage());
      throw e;
    } finally {
      system.shutdown();
      system.awaitTermination();
    }
  }
}
