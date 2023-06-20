package com.inrupt.client.integration.base;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;

import java.util.Map;

public class AccessGrantServerRevokeTransformer extends ResponseDefinitionTransformer {

    private final Map<String, String> grantStatus;
    public AccessGrantServerRevokeTransformer(Map<String, String> grantStatus) {
        this.grantStatus = grantStatus;
    }

    @Override
    public String getName() {
        return "Access Grant Server Revoke";
    }

    @Override
    public ResponseDefinition transform(final Request request, final ResponseDefinition responseDefinition,
                                        final FileSource files, final Parameters parameters) {

        final var res = new ResponseDefinitionBuilder();

        final String grant = (String) parameters.get("grant");

        res.withStatus(Utils.SUCCESS)
                .withHeader(Utils.CONTENT_TYPE, Utils.APPLICATION_JSON)
                .withBody(grant);
        grantStatus.put(grant, "revoked");
        return res.build();
    }

}
