/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.aws.apigateway.openapi;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import software.amazon.smithy.aws.traits.apigateway.RequestValidatorTrait;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.OperationShape;
import software.amazon.smithy.openapi.fromsmithy.Context;
import software.amazon.smithy.openapi.fromsmithy.OpenApiMapper;
import software.amazon.smithy.openapi.model.OpenApi;
import software.amazon.smithy.openapi.model.OperationObject;
import software.amazon.smithy.utils.MapUtils;
import software.amazon.smithy.utils.OptionalUtils;

/**
 * Adds the API Gateway x-amazon-apigateway-request-validators object
 * to the service and x-amazon-apigateway-request-validator to the
 * service/operations.
 *
 * <p>Any operation or service shape with the {@link RequestValidatorTrait}
 * applied to it will cause that operation to have the {@code x-amazon-apigateway-request-validator}
 * extension, and adds a {@code x-amazon-apigateway-request-validators} extension
 * to the top-level OpenAPI document.
 *
 * @see <a href="https://docs.aws.amazon.com/apigateway/latest/developerguide/api-gateway-swagger-extensions-request-validators.html">Request validators</a>
 */
final class AddRequestValidators implements OpenApiMapper {
    private static final String REQUEST_VALIDATOR = "x-amazon-apigateway-request-validator";
    private static final String REQUEST_VALIDATORS = "x-amazon-apigateway-request-validators";
    private static final Map<String, Node> KNOWN_VALIDATORS = MapUtils.of(
            "params-only",
            Node.objectNode().withMember("validateRequestParameters", Node.from(true)),
            "body-only",
            Node.objectNode().withMember("validateRequestBody", Node.from(true)),
            "full",
            Node.objectNode()
                    .withMember("validateRequestParameters", Node.from(true))
                    .withMember("validateRequestBody", Node.from(true))
    );

    @Override
    public OperationObject updateOperation(Context context, OperationShape shape, OperationObject operation) {
        return shape.getTrait(RequestValidatorTrait.class)
                .map(RequestValidatorTrait::getValue)
                .map(value -> operation.toBuilder().putExtension(REQUEST_VALIDATOR, value).build())
                .orElse(operation);
    }

    @Override
    public OpenApi after(Context context, OpenApi openapi) {
        // Find each known request validator on operation shapes.
        Set<String> validators = context.getModel().getShapeIndex().shapes(OperationShape.class)
                .flatMap(shape -> OptionalUtils.stream(shape.getTrait(RequestValidatorTrait.class)))
                .map(RequestValidatorTrait::getValue)
                .filter(KNOWN_VALIDATORS::containsKey)
                .collect(Collectors.toSet());

        // Check if the service has a request validator.
        String serviceValidator = null;
        if (context.getService().getTrait(RequestValidatorTrait.class).isPresent()) {
            serviceValidator = context.getService().getTrait(RequestValidatorTrait.class).get().getValue();
            validators.add(serviceValidator);
        }

        if (validators.isEmpty()) {
            return openapi;
        }

        OpenApi.Builder builder = openapi.toBuilder();

        if (serviceValidator != null) {
            builder.putExtension(REQUEST_VALIDATOR, serviceValidator);
        }

        // Add the known request validators to the OpenAPI model.
        ObjectNode.Builder objectBuilder = Node.objectNodeBuilder();
        for (String validator : validators) {
            objectBuilder.withMember(validator, KNOWN_VALIDATORS.get(validator));
        }

        builder.putExtension(REQUEST_VALIDATORS, objectBuilder.build());
        return builder.build();
    }
}
