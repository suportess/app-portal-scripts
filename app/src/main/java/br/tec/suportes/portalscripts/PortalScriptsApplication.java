package br.tec.suportes.portalscripts;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
public class PortalScriptsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PortalScriptsApplication.class, args);
    }
}
