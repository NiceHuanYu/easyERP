package top.huanyu666.backend.common.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 将 Knife4j 的 /v2/api-docs 请求转发到 SpringDoc /v3/api-docs
 */
@Controller
public class SwaggerRedirectController {

    @GetMapping("/v2/api-docs")
    public String redirectToV3() {
        return "forward:/v3/api-docs";
    }
}
