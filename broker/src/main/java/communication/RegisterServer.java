package communication;

public interface RegisterServer {

    void start();

    void registerEntities();

    void closeRegister();

    void closeSockets();
}
