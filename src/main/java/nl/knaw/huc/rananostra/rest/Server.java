package nl.knaw.huc.rananostra.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class Server extends Application<Server.Config> {
  public static class Config extends Configuration {
    @JsonProperty
    public String host;

    @JsonProperty
    public int port;
  }

  @Override
  public String getName() {
    return "rananostra";
  }

  @Override
  public void initialize(Bootstrap<Config> bootstrap) {
  }

  public static void main(String[] args) throws Exception {
    new Server().run(args);
  }

  @Override
  public void run(Config conf, Environment env) throws Exception {
    env.jersey().register(new FrogResource(conf.host, conf.port));
  }
}
