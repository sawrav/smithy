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

package software.amazon.smithy.model.neighbor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.Set;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.shapes.ListShape;
import software.amazon.smithy.model.shapes.MapShape;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeIndex;
import software.amazon.smithy.model.shapes.StringShape;

public class WalkerTest {

    @Test
    public void getASetOfConnectedShapes() {
        // list-of-map-of-string-to-string
        StringShape string = StringShape.builder()
                .id("ns.foo#String")
                .build();
        MemberShape key = MemberShape.builder()
                .id("ns.foo#Map$key")
                .target(string.getId())
                .build();
        MemberShape value = MemberShape.builder()
                .id("ns.foo#Map$value")
                .target(string.getId())
                .build();
        MapShape map = MapShape.builder()
                .id("ns.foo#Map")
                .key(key)
                .value(value)
                .build();
        MemberShape listMember = MemberShape.builder()
                .id("ns.foo#List$member")
                .target(map.getId())
                .build();
        ListShape list = ListShape.builder()
                .id("ns.foo#List")
                .member(listMember)
                .build();
        Walker walker = new Walker(ShapeIndex.builder()
                .addShape(list)
                .addShape(listMember)
                .addShape(map)
                .addShape(key)
                .addShape(value)
                .addShape(string)
                .build());
        Set<Shape> connected = walker.walkShapes(listMember);

        assertThat(connected, containsInAnyOrder(list, listMember, map, key, value, string));
    }

    @Test
    public void supportsCycles() {
        // list-of-map-of-string-to-list-of-...
        StringShape string = StringShape.builder()
                .id("ns.foo#String")
                .build();
        MemberShape key = MemberShape.builder()
                .id("ns.foo#Map$key")
                .target(string.getId())
                .build();
        MemberShape value = MemberShape.builder()
                .id("ns.foo#Map$value")
                .target("ns.foo#List") // cycles here
                .build();
        MapShape map = MapShape.builder()
                .id("ns.foo#Map")
                .key(key)
                .value(value)
                .build();
        MemberShape listMember = MemberShape.builder()
                .id("ns.foo#List$member")
                .target(map.getId())
                .build();
        ListShape list = ListShape.builder()
                .id("ns.foo#List")
                .member(listMember)
                .build();
        Walker walker = new Walker(ShapeIndex.builder()
                .addShape(list)
                .addShape(listMember)
                .addShape(map)
                .addShape(key)
                .addShape(value)
                .addShape(string)
                .build());
        Set<Shape> connected = walker.walkShapes(listMember);

        assertThat(connected, containsInAnyOrder(list, listMember, map, key, value, string));
    }
}
