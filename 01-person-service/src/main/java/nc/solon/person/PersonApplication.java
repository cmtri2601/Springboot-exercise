package nc.solon.person;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * The type Person application.
 */
@SpringBootApplication
@ComponentScan(basePackages = {
        "nc.solon.person",
        "nc.solon.common"
})
public class PersonApplication {

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(PersonApplication.class, args);
    }
}
