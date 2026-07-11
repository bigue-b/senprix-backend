package sn.dci.senprix.produit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure la documentation Swagger/OpenAPI du produit-service,
 * avec prise en charge de l'authentification Bearer JWT pour
 * tester les endpoints admin directement depuis l'interface Swagger UI.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SEN-PRIX — Produit Service API")
                        .description("Référentiel des produits de première nécessité et des marchés couverts par la DCI")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Direction du Commerce Intérieur — Sénégal")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
