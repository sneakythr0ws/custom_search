package org.nick.util.customsearch.config;

import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by vladimirn on 23.12.16.
 */
@Configuration
@Profile({"SWAGGER"})
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        Predicate<String> paths = PathSelectors.any();
        return new Docket(DocumentationType.SWAGGER_2)
                .directModelSubstitute(LocalDate.class, String.class)
                //.globalOperationParameters(getOperationParameters(SECURITY_HEADER_USER, ContentToolAuthFilter.SECURITY_HEADER_API_KEY, SECURITY_HEADER_PROJECT))
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(paths)
                .build()
                .pathMapping("/");
    }

    private List<Parameter> getOperationParameters(String... headersNames) {
        return Arrays.stream(headersNames).map(this::buildHeader).collect(Collectors.toList());
    }


    private Parameter buildHeader(String headerName) {
        return new ParameterBuilder()
                .name(headerName)
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                //.defaultValue(UserInfo.DEFAULT_USER)
                .build();
    }
}