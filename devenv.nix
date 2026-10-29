# authentication-service/devenv.nix
{pkgs, ...}: let
  application_name = "authentication-service";
  database_name = "authentication-service";
  database_username = "jarsolutions";
  database_password = "jarsolutions";
  database_host = "127.0.0.1";
  # Give every project its own ports, so two projects can run at the same time
  database_port = 5432;
  server_port = 8080;
  debug_port = 5005;
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
  # Spring Boot reads SERVER_PORT as server.port
  env.SERVER_PORT = server_port;
  # Read by the server script below and by nvim-java's DAP attach
  env.debug_port = debug_port;

  env.allowed-origins = allowed-origins;

  packages = with pkgs; [
    spring-boot-cli
    pgcli
    liquibase
  ];

  # jdk21 from the nixpkgs pinned in devenv.yaml (kukenan's pin) is the same
  # store path nvim-java's jdtls runs on, so the project adds no JDK on disk
  languages.java = {
    enable = true;
    jdk.package = pkgs.jdk21;
    # Maven built against the JDK above, instead of its own openjdk-headless
    maven.enable = true;
    # nvim-java ships its own jdtls
    lsp.enable = false;
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

  # Waits until `devenv up` has Postgres accepting connections, so the tmux
  # windows need no sleeps
  scripts.wait-for-db.exec = ''
    until pg_isready -q -h "$database_host" -p "$database_port"; do sleep 0.5; done
  '';

  # The app, with the heap capped and the debugger listening on debug_port
  scripts.server.exec = ''
    wait-for-db
    mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx512m -XX:TieredStopAtLevel=1 -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$debug_port"
  '';

  scripts.db.exec = ''
    wait-for-db
    pgcli "postgresql://$database_username:$database_password@$database_host:$database_port/$database_name"
  '';

  enterShell = ''
    echo "--- $GREET ---"
    java -version
    mvn -version
    echo "------------------------------------------------"
    echo "DB: postgresql://${database_host}:${toString database_port}/${database_name}"
    echo "App: http://localhost:${toString server_port}  debug: ${toString debug_port}"
    echo "------------------------------------------------"
  '';
}
