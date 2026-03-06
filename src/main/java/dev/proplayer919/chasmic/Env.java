package dev.proplayer919.chasmic;

public abstract class Env {
    public static String env() {
        return System.getenv().getOrDefault("CHASMIC_ENV", "dev");
    }

    public static String getMongoUri() {
        return System.getenv().getOrDefault("MONGO_URI", "mongodb://localhost:27017");
    }

    public static boolean isProd() {
        return "prod".equalsIgnoreCase(env());
    }

    public static boolean isDev() {
        return "dev".equalsIgnoreCase(env());
    }
}
