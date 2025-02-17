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

package software.amazon.smithy.model.validation.node;

import java.util.List;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeIndex;

/**
 * Applies pluggable validation when validating {@link Node} values against
 * the schema of a {@link Shape} (e.g., when validating that the values
 * provided for a trait in the model are valid for the shape of the trait).
 */
public interface NodeValidatorPlugin {
    /**
     * Applies the plugin to the given shape, node value, and shape index.
     *
     * @param shape Shape being checked.
     * @param value Value being evaluated.
     * @param index Shape index used to traverse the model.
     * @return Returns any validation messages that were encountered.
     */
    List<String> apply(Shape shape, Node value, ShapeIndex index);
}
