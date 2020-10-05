package com.github.gdl;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
        info = @Info(
                title = "Direct print service",
                description = "Direct print to client local printer API Documentation",
                version = "v0.0.1",
                license = @License(
                        name = "Apache License Version 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                )
        )
)
public class OpenAPIConfig {
}
