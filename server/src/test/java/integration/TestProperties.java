package integration;

import java.io.IOException;
import java.util.Properties;

public class TestProperties {

    private Properties properties;
    static private TestProperties instance;

    private TestProperties() {
        properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("application-test.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Something wrong with properties",e);
        }
    }

    public synchronized static TestProperties getInstance(){
        if (instance == null){
            instance = new TestProperties();
        }
        return instance;
    }

    public Integer getAppPort(){
        return Integer.parseInt(properties.getProperty("server.port"));
    }

    public String getAppHost(){
        return properties.getProperty("server.host");
    }



}
