package nc.solon.person.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * The type Api prefix config.
 */
@Configuration
public class ApiPrefixConfig implements WebMvcConfigurer {

    @Value("${server.prefix}")
    private String prefix;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer
                .setPatternParser(new PathPatternParser())
                .addPathPrefix(prefix, c -> true); // Apply to all controllers
    }
}
