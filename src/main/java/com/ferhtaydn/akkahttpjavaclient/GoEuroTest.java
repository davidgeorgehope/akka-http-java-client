package com.ferhtaydn.akkahttpjavaclient;

import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.IncomingConnection;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.japi.function.Function;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.ferhtaydn.akkahttpjavaclient.client.AkkaHttpJavaClient;
import com.ferhtaydn.akkahttpjavaclient.helpers.CsvHelper;
import com.ferhtaydn.akkahttpjavaclient.models.City;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class GoEuroTest {

    /*public static void main(String[] args) {
    try {


        //#binding-example
        ActorSystem system = ActorSystem.create();
        Materializer materializer = ActorMaterializer.create(system);

        Source<IncomingConnection, CompletionStage<ServerBinding>> serverSource =
                Http.get(system).bind(ConnectHttp.toHost("localhost", 8080), materializer);

        CompletionStage<ServerBinding> serverBindingFuture =
                serverSource.to(Sink.foreach(connection -> {
                            System.out.println("Accepted new connection from " + connection.remoteAddress());
                            // ... and then actually handle the connection
                            getCity(client);

                        }
                )).run(materializer);
        //#binding-example
        serverBindingFuture.toCompletableFuture().get(3, TimeUnit.SECONDS);

    }catch(Exception e){

    }

    }*/

    public static void fullServerExample() throws Exception {
        //#full-server-example
        ActorSystem system = ActorSystem.create();
        //ActorSystem actorSystem = ActorSystem.create("client");
        AkkaHttpJavaClient client = new AkkaHttpJavaClient(system, ActorMaterializer.create(system));
        //#full-server-example
        try {
            //#full-server-example
            final Materializer materializer = ActorMaterializer.create(system);

            Source<IncomingConnection, CompletionStage<ServerBinding>> serverSource =
                    Http.get(system).bind(ConnectHttp.toHost("localhost", 8080), materializer);

            //#request-handler
            final Function<HttpRequest, HttpResponse> requestHandler =
                    new Function<HttpRequest, HttpResponse>() {
                        private final HttpResponse NOT_FOUND =
                                HttpResponse.create()
                                        .withStatus(404)
                                        .withEntity("Unknown resource!");


                        @Override
                        public HttpResponse apply(HttpRequest request)  throws Exception{
                            Uri uri = request.getUri();
                            if (request.method() == HttpMethods.GET) {
                                if (uri.path().equals("/")) {
                                    return
                                            HttpResponse.create()
                                                    .withEntity(ContentTypes.TEXT_HTML_UTF8,
                                                            "<html><body>Hello world!</body></html>");
                                } else if (uri.path().equals("/city")) {
                                    String name = uri.query().get("city").orElse("London");
                                    getCity(client,name);


                                    return
                                            HttpResponse.create()
                                                    .withEntity("Hello " + name + "!");
                                } else if (uri.path().equals("/ping")) {
                                    return HttpResponse.create().withEntity("PONG!");
                                } else {
                                    return NOT_FOUND;
                                }
                            } else {
                                return NOT_FOUND;
                            }
                        }
                    };
            //#request-handler

            CompletionStage<ServerBinding> serverBindingFuture =
                    serverSource.to(Sink.foreach(connection -> {
                        System.out.println("Accepted new connection from " + connection.remoteAddress());

                        connection.handleWithSyncHandler(requestHandler, materializer);
                        // this is equivalent to
                        //connection.handleWith(Flow.of(HttpRequest.class).map(requestHandler), materializer);
                    })).run(materializer);
            //#full-server-example

            serverBindingFuture.toCompletableFuture().get(3, TimeUnit.SECONDS); // will throw if binding fails
            System.out.println("Press ENTER to stop.");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } finally {
            system.terminate();
        }
    }

    public static void main(String[] args) throws Exception {
        fullServerExample();
    }

    private static void getCity(AkkaHttpJavaClient client,String cityName) {

        System.out.println(cityName);
        if (cityName != null && !cityName.isEmpty()) {

            client.requestLevelFutureBased(Optional.of(cityName), success ->
                    Jackson.unmarshaller(City[].class)
                            .unmarshall(success.entity(),
                                    client.getSystem().dispatcher(),
                                    client.getMaterializer())
                            .thenApply(Arrays::asList))

                    .thenAccept(cities -> new CsvHelper().writeToCSV(cities, cityName))
                    .whenComplete((success, throwable) -> System.out.println("Enter country"));
        } else {
            System.err.println("Enter a city name!");
        }
    }

}
