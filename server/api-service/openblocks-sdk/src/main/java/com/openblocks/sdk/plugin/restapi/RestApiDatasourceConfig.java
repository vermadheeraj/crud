package com.openblocks.sdk.plugin.restapi;

import static com.openblocks.sdk.exception.BizError.INVALID_DATASOURCE_CONFIG_TYPE;
import static com.openblocks.sdk.util.ExceptionUtils.ofException;
import static com.openblocks.sdk.util.ExceptionUtils.ofPluginException;
import static com.openblocks.sdk.util.JsonUtils.fromJson;
import static com.openblocks.sdk.util.JsonUtils.toJson;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.collections4.SetUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.openblocks.sdk.exception.PluginCommonError;
import com.openblocks.sdk.models.DatasourceConnectionConfig;
import com.openblocks.sdk.models.Property;
import com.openblocks.sdk.plugin.common.ssl.SslConfig;
import com.openblocks.sdk.plugin.restapi.auth.AuthConfig;
import com.openblocks.sdk.plugin.restapi.auth.RestApiAuthType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class RestApiDatasourceConfig implements DatasourceConnectionConfig {

    public static final RestApiDatasourceConfig EMPTY_CONFIG = RestApiDatasourceConfig.builder().build();

    private final List<Property> params;
    private final String body;
    private final List<Property> bodyFormData;
    private final List<Property> headers;
    private final String url;
    private final Set<String> forwardCookies;
    private final boolean forwardAllCookies;
    @Getter
    @Setter
    @Nullable
    private AuthConfig authConfig;
    @Getter
    @Setter
    private SslConfig sslConfig;

    @JsonCreator
    private RestApiDatasourceConfig(List<Property> params, String body, List<Property> bodyFormData, List<Property> headers,
            String url, Set<String> forwardCookies, boolean forwardAllCookies, @Nullable AuthConfig authConfig, SslConfig sslConfig) {
        this.params = params;
        this.body = body;
        this.bodyFormData = bodyFormData;
        this.headers = headers;
        this.url = url;
        this.forwardCookies = forwardCookies;
        this.forwardAllCookies = forwardAllCookies;
        this.authConfig = authConfig;
        this.sslConfig = sslConfig;
    }

    public static RestApiDatasourceConfig buildFrom(Map<String, Object> requestMap) {
        RestApiDatasourceConfig result = fromJson(toJson(requestMap), RestApiDatasourceConfig.class);
        if (result == null) {
            throw ofPluginException(PluginCommonError.DATASOURCE_ARGUMENT_ERROR, "INVALID_RESTAPI_CONFIG");
        }
        return result;
    }

    public static RestApiDatasourceConfig from(DatasourceConnectionConfig datasourceConfig) {

        if (datasourceConfig instanceof RestApiDatasourceConfig config) {
            return config;
        }

        return null;
    }

    public List<Property> getHeaders() {
        return emptyIfNull(headers);
    }

    public List<Property> getParams() {
        return emptyIfNull(params);
    }

    public List<Property> getBodyFormData() {
        return emptyIfNull(bodyFormData);
    }

    public String getBody() {
        return body;
    }

    public String getUrl() {
        return trimToEmpty(url);
    }

    public RestApiAuthType getAuthType() {
        if (this.authConfig != null) {
            return this.authConfig.getType();
        }
        //default
        return RestApiAuthType.NO_AUTH;
    }

    public Set<String> getForwardCookies() {
        return SetUtils.emptyIfNull(forwardCookies);
    }

    public boolean isForwardAllCookies() {
        return forwardAllCookies;
    }

    @Override
    public DatasourceConnectionConfig mergeWithUpdatedConfig(DatasourceConnectionConfig updatedConfig) {
        if (!(updatedConfig instanceof RestApiDatasourceConfig updatedApiConfig)) {
            throw ofException(INVALID_DATASOURCE_CONFIG_TYPE, "INVALID_DATASOURCE_CONFIG_TYPE", updatedConfig.getClass().getSimpleName());
        }
        if (this.authConfig != null) {
            updatedApiConfig.setAuthConfig(this.authConfig.mergeWithUpdatedConfig(updatedApiConfig.getAuthConfig()));
        }
        if (this.sslConfig != null) {
            updatedApiConfig.setSslConfig(this.sslConfig.mergeWithUpdatedConfig(updatedApiConfig.getSslConfig()));
        }
        return updatedApiConfig;
    }

    @Override
    public DatasourceConnectionConfig doEncrypt(Function<String, String> encryptFunc) {
        if (this.authConfig != null) {
            this.authConfig.doEncrypt(encryptFunc);
        }
        if (this.sslConfig != null) {
            this.sslConfig.doEncrypt(encryptFunc);
        }
        return this;
    }

    @Override
    public DatasourceConnectionConfig doDecrypt(Function<String, String> decryptFunc) {
        if (this.authConfig != null) {
            this.authConfig.doDecrypt(decryptFunc);
        }
        if (this.sslConfig != null) {
            this.sslConfig.doDecrypt(decryptFunc);
        }
        return this;
    }
}
