package br.com.ricardobpedro.amazons3.soap;

import javax.xml.ws.Endpoint;

/**
 * Created by ricardo on 15/01/17.
 */
public class Bootstrap {

    public static void main(String[] args) {

        Soap service = new Soap();
        String url = "http://0.0.0.0:8081/s3";

        Endpoint.publish(url, service);
    }
}
