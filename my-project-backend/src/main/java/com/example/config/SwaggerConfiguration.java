package com.example.config;

import com.example.entity.RestBean;
import com.example.entity.vo.response.AuthorizeVO;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.QueryParameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Swagger开发文档相关配置
 */
@Configuration
@SecurityScheme(in = SecuritySchemeIn.HEADER, name = "Authorization",
        type = SecuritySchemeType.HTTP, scheme = "Bearer")
@OpenAPIDefinition(security = { @SecurityRequirement(name = "Authorization") }) // 自动给接口请求添加认证
public class SwaggerConfiguration {

    /**
     * 配置文档介绍以及详细信息
     * @return OpenAPI
     */
    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("校园论坛 API 文档")
                        .description("欢迎来到本示例项目API测试文档，在这里可以快速进行接口调试")
                        .version("1.0")
                )
                .externalDocs(new ExternalDocumentation()
                        .description("项目开源地址")
                        .url("https://github.com/MapleChenX/forum-jwt")
                );
    }

    /**
     * 配置自定义的OpenApi相关信息
     * @return OpenApiCustomizer
     */
    @Bean
    public OpenApiCustomizer customerGlobalHeaderOpenApiCustomizer() {
        return api -> this.authorizePathItems().forEach(api.getPaths()::addPathItem);
    }

    /**
     * 登录接口和退出登录接口手动添加一下
     * @return PathItems
     */
    private Map<String, PathItem> authorizePathItems(){
        Map<String, PathItem> map = new HashMap<>();
        map.put("/api/auth/login", new PathItem()
                .post(new Operation()
                        .tags(List.of("登录校验相关"))
                        .summary("登录验证接口")
                        .addParametersItem(new QueryParameter()
                                .name("username")
                                .required(true)
                                .content(new Content().addMediaType("application/json", new MediaType()))
                        )
                        .addParametersItem(new QueryParameter()
                                .name("password")
                                .required(true)
                                .content(new Content().addMediaType("application/json", new MediaType()))
                        )
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        .content(new Content().addMediaType("*/*", new MediaType()
                                                .example(RestBean.success(new AuthorizeVO()).asJsonString())
                                        ))
                                )
                        )
                )
        );
        map.put("/api/auth/logout", new PathItem()
                .get(new Operation()
                        .tags(List.of("登录校验相关"))
                        .summary("退出登录接口")
                        .responses(new ApiResponses()
                                .addApiResponse("200", new ApiResponse()
                                        .description("OK")
                                        .content(new Content().addMediaType("*/*", new MediaType()
                                                .example(RestBean.success())
                                        ))
                                )
                        )
                )

        );
        return map;
    }
}
