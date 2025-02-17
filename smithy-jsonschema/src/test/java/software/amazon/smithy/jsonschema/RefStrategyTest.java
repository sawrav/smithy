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

package software.amazon.smithy.jsonschema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;

public class RefStrategyTest {
    @Test
    public void defaultImplUsesDefaultPointer() {
        RefStrategy ref = RefStrategy.createDefaultStrategy();
        String pointer = ref.toPointer(ShapeId.from("smithy.example#Foo"), Node.objectNode());

        assertThat(pointer, equalTo("#/definitions/SmithyExampleFoo"));
    }

    @Test
    public void defaultImplUsesCustomPointerAndAppendsSlashWhenNecessary() {
        RefStrategy ref = RefStrategy.createDefaultStrategy();
        ObjectNode config = Node.objectNodeBuilder()
                .withMember(JsonSchemaConstants.DEFINITION_POINTER, Node.from("#/components/schemas"))
                .build();
        String pointer = ref.toPointer(ShapeId.from("smithy.example#Foo"), config);

        assertThat(pointer, equalTo("#/components/schemas/SmithyExampleFoo"));
    }

    @Test
    public void defaultImplUsesCustomPointerAndOmitsSlashWhenNecessary() {
        RefStrategy ref = RefStrategy.createDefaultStrategy();
        ObjectNode config = Node.objectNodeBuilder()
                .withMember(JsonSchemaConstants.DEFINITION_POINTER, Node.from("#/components/schemas"))
                .build();
        String pointer = ref.toPointer(ShapeId.from("smithy.example#Foo"), config);

        assertThat(pointer, equalTo("#/components/schemas/SmithyExampleFoo"));
    }

    @Test
    public void defaultImplStripsNamespacesWhenRequested() {
        RefStrategy ref = RefStrategy.createDefaultStrategy();
        ObjectNode config = Node.objectNodeBuilder()
                .withMember(JsonSchemaConstants.SMITHY_STRIP_NAMESPACES, true)
                .build();
        String pointer = ref.toPointer(ShapeId.from("smithy.example#Foo"), config);

        assertThat(pointer, equalTo("#/definitions/Foo"));
    }

    @Test
    public void defaultImplAddsRefWhenMember() {
        RefStrategy ref = RefStrategy.createDefaultStrategy();
        String pointer = ref.toPointer(ShapeId.from("smithy.example#Foo$bar"), Node.objectNode());

        assertThat(pointer, equalTo("#/definitions/SmithyExampleFooBarMember"));
    }
}
