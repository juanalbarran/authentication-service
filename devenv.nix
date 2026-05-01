# java/devenv.nix
{pkgs, ...}: let
  application_name = "authentication-service";
  database_name = "authentication-service";
  database_username = "jarsolutions";
  database_password = "jarsolutions";
  database_host = "127.0.0.1";
  database_port = 5432;
  allowed-origins = "http://localhost:3000";
in {
  # https://devenv.sh/basics/
  env.GREET = "Java & Spring Boot Development";
  env.application_name = application_name;
  env.database_name = database_name;
  env.database_username = database_username;
  env.database_password = database_password;
  env.database_host = database_host;
  env.database_port = database_port;

  env.allowed-origins = allowed-origins;

  packages = with pkgs; [
    maven
    spring-boot-cli
    pgcli
    liquibase
  ];

  # Enable Java (Temurin 21 is the current standard for modern Spring Boot)
  languages.java = {
    enable = true;
    jdk.package = pkgs.temurin-bin-21;
  };

  # Local PostgreSQL 18 configuration
  services.postgres = {
    enable = true;
    package = pkgs.postgresql_18;
    initialDatabases = [{name = database_name;}];
    listen_addresses = database_host;
    port = database_port;
    initialScript = ''
      CREATE USER ${database_username} WITH PASSWORD '${database_password}' SUPERUSER;
      ALTER DATABASE ${database_name} OWNER TO ${database_username};
    '';
  };

  enterShell = ''
    echo "--- $GREET ---"
    java -version
    mvn -version
    echo "------------------------------------------------"
    echo "DB: postgresql://${database_host}:${toString database_port}/${database_name}"
    echo "------------------------------------------------"
  '';
}
