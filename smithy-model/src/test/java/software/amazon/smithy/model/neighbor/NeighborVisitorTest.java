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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import java.util.List;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.shapes.BlobShape;
import software.amazon.smithy.model.shapes.BooleanShape;
import software.amazon.smithy.model.shapes.ListShape;
import software.amazon.smithy.model.shapes.MapShape;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.OperationShape;
import software.amazon.smithy.model.shapes.ResourceShape;
import software.amazon.smithy.model.shapes.ServiceShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.ShapeIndex;
import software.amazon.smithy.model.shapes.StringShape;
import software.amazon.smithy.model.shapes.StructureShape;
import software.amazon.smithy.model.shapes.TimestampShape;
import software.amazon.smithy.model.shapes.UnionShape;
import software.amazon.smithy.model.traits.IdempotentTrait;
import software.amazon.smithy.model.traits.ReadonlyTrait;

public class NeighborVisitorTest {

    @Test
    public void blobShape() {
        Shape shape = BlobShape.builder().id("ns.foo#name").build();
        ShapeIndex shapeIndex = ShapeIndex.builder().addShape(shape).build();
        NeighborVisitor neighborVisitor = new NeighborVisitor(shapeIndex);
        List<Relationship> relationships = shape.accept(neighborVisitor);

        assertThat(relationships, empty());
    }

    @Test
    public void booleanShape() {
        Shape shape = BooleanShape.builder().id("ns.foo#name").build();
        ShapeIndex shapeIndex = ShapeIndex.builder().addShape(shape).build();
        NeighborVisitor neighborVisitor = new NeighborVisitor(shapeIndex);
        List<Relationship> relationships = shape.accept(neighborVisitor);

        assertThat(relationships, empty());
    }

    @Test
    public void stringShape() {
        Shape shape = StringShape.builder().id("ns.foo#name").build();
        ShapeIndex shapeIndex = ShapeIndex.builder().addShape(shape).build();
        NeighborVisitor neighborVisitor = new NeighborVisitor(shapeIndex);
        List<Relationship> relationships = shape.accept(neighborVisitor);

        assertThat(relationships, empty());
    }

    @Test
    public void timestampShape() {
        Shape shape = TimestampShape.builder().id("ns.foo#name").build();
        ShapeIndex shapeIndex = ShapeIndex.builder().addShape(shape).build();
        NeighborVisitor neighborVisitor = new NeighborVisitor(shapeIndex);
        List<Relationship> relationships = shape.accept(neighborVisitor);

        assertThat(relationships, empty());
    }

    @Test
    public void listShape() {
        Shape string = StringShape.builder().id("ns.foo#String").build();
        ListShape list = ListShape.builder()
                .id("ns.foo#name")
                .member(MemberShape.builder()
                        .id("ns.foo#name$member")
                        .target(string.getId())
                        .build())
                .build();
        ShapeIndex shapeIndex = ShapeIndex.builder().addShape(list).addShape(string).build();
        NeighborVisitor neighborVisitor = new NeighborVisitor(shapeIndex);
        MemberShape memberTarget = list.getMember();
        List<Relationship> relationships = list.accept(neighborVisitor);

        assertThat(relationships, contains(Relationship.create(list, RelationshipType.LIST_MEMBER, memberTarget)));
    }

    @Test
    public void mapShape() {
        Shape key = StringShape.builder().id("ns.foo#KeyString").build();
        Shape value = StringShape.builder().id("ns.foo#ValueString").build();
        MapShape map = MapShape.builder()
                .id("ns.foo#name")
                .key(MemberShape.builder()
                        .id("ns.foo#name$key")
                        .target(key.getId())
                        .build())
                .value(MemberShape.builder()
                        .id("ns.foo#name$value")
                        .target(value.getId())
                        .build())
                .build();
        MemberShape keyTarget = map.getKey();
        MemberShape valueTarget = map.getValue();
        ShapeIndex shapeIndex = ShapeIndex.builder().addShape(map).addShape(key).addShape(value).build();
        NeighborVisitor neighborVisitor = new NeighborVisitor(shapeIndex);
        List<Relationship> relationships = map.accept(neighborVisitor);

        assertThat(relationships, containsInAnyOrder(
                Relationship.create(map, RelationshipType.MAP_KEY, keyTarget),
                Relationship.create(map, RelationshipType.MAP_VALUE, valueTarget)));
    }

    @Test
    public void structureShape() {
        Shape memberShape1 = StringShape.builder().id("ns.foo#M1String").build();
        Shape memberShape2 = StringShape.builder().id("ns.foo#M2String").build();
        StructureShape struct = StructureShape.builder()
                .id("ns.foo#name")
                .addMember(MemberShape.builder()
                        .id("ns.foo#name$m1")
                        .target(memberShape1.getId())
                        .build())
                .addMember(MemberShape.builder()
                        .id("ns.foo#name$m2")
                        .target(memberShape2.getId())
                        .build())
                .build();
        MemberShape member1Target = struct.getMember("m1").get();
        MemberShape member2Target = struct.getMember("m2").get();
        ShapeIndex shapeIndex = ShapeIndex.builder()
                .addShape(struct)
                .addShape(memberShape1)
                .addShape(memberShape2)
                .addShape(member1Target)
                .addShape(member2Target)
                .build();
        NeighborVisitor neighborVisitor = new NeighborVisitor(shapeIndex);
        List<Relationship> relationships = struct.accept(neighborVisitor);

        assertThat(relationships, containsInAnyOrder(
                Relationship.create(struct, RelationshipType.STRUCTURE_MEMBER, member1Target),
                Relationship.create(struct, RelationshipType.STRUCTURE_MEMBER, member2Target)));
    }

    @Test
    public void unionShape() {
        Shape variant1Shape = StringShape.builder().id("ns.foo#V1String").build();
        Shape variant2Shape = StringShape.builder().id("ns.foo#V2String").build();
        UnionShape union = UnionShape.builder()
                .id("ns.foo#name")
                .addMember(MemberShape.builder()
                        .id("ns.foo#name$tag1")
                        .target(variant1Shape.getId())
                        .build())
                .addMember(MemberShape.builder()
                        .id("ns.foo#name$tag2")
                        .target(variant2Shape.getId())
                        .build())
                .build();
        MemberShape v1Target = union.getMember("tag1").get();
        MemberShape v2Target = union.getMember("tag2").get();
        ShapeIndex shapeIndex = ShapeIndex.builder()
                .addShape(union)
                .addShape(variant1Shape)
                .addShape(variant2Shape)
                .addShape(v1Target)
                .addShape(v2Target)
                .build();
        NeighborVisitor neighborVisitor = new NeighborVisitor(shapeIndex);
        List<Relationship> relationships = union.accept(neighborVisitor);

        assertThat(relationships, containsInAnyOrder(
                Relationship.create(union, RelationshipType.UNION_MEMBER, v1Target),
                Relationship.create(union, RelationshipType.UNION_MEMBER, v2Target)));
    }

    @Test
    public void serviceShape() {
        ServiceShape service = ServiceShape.builder()
                .id("ns.foo#Svc")
                .version("2017-01-17")
                .addOperation("ns.foo#Operation")
                .addResource("ns.foo#Resource")
                .build();
        OperationShape operationShape = OperationShape.builder().id("ns.foo#Operation").build();
        ResourceShape resourceShape = ResourceShape.builder().id("ns.foo#Resource").build();
        ShapeIndex shapeIndex = ShapeIndex.builder()
                .addShapes(service, resourceShape, operationShape)
                .build();
        NeighborVisitor neighborVisitor = new NeighborVisitor(shapeIndex);
        List<Relationship> relationships = service.accept(neighborVisitor);

        assertThat(relationships, containsInAnyOrder(
                Relationship.create(service, RelationshipType.RESOURCE, resourceShape),
                Relationship.create(service, RelationshipType.OPERATION, operationShape),
                Relationship.create(resourceShape, RelationshipType.BOUND, service),
                Relationship.create(operationShape, RelationshipType.BOUND, service)));
    }

    @Test
    public void resourceShape() {
        ServiceShape parent = ServiceShape.builder()
                .id("ns.foo#Svc")
                .version("2017-01-17")
                .addResource("ns.foo#Resource")
                .build();
        ResourceShape resource = ResourceShape.builder()
                .id("ns.foo#Resource")
                .addIdentifier("id", "ns.foo#ResourceIdentifier")
                .put(ShapeId.from("ns.foo#Put"))
                .create(ShapeId.from("ns.foo#Create"))
                .read(ShapeId.from("ns.foo#Get"))
                .update(ShapeId.from("ns.foo#Update"))
                .delete(ShapeId.from("ns.foo#Delete"))
                .list(ShapeId.from("ns.foo#List"))
                .addOperation("ns.foo#Named")
                .addCollectionOperation("ns.foo#GenericCollection")
                .addResource("ns.foo#Child1")
                .build();
        StringShape identifier = StringShape.builder().id("ns.foo#ResourceIdentifier").build();
        OperationShape createOperation = OperationShape.builder().id("ns.foo#Create").build();
        OperationShape getOperation = OperationShape.builder()
                .id("ns.foo#Get")
                .addTrait(new ReadonlyTrait(SourceLocation.NONE))
                .build();
        OperationShape updateOperation = OperationShape.builder().id("ns.foo#Update").build();
        OperationShape deleteOperation = OperationShape.builder()
                .id("ns.foo#Delete")
                .addTrait(new IdempotentTrait(SourceLocation.NONE))
                .build();
        OperationShape listOperation = OperationShape.builder()
                .id("ns.foo#List")
                .addTrait(new ReadonlyTrait(SourceLocation.NONE))
                .build();
        OperationShape namedOperation = OperationShape.builder()
                .id("ns.foo#Named")
                .build();
        OperationShape collectionOperation = OperationShape.builder()
                .id("ns.foo#GenericCollection")
                .build();
        OperationShape putOperation = OperationShape.builder()
                .id("ns.foo#Put")
                .build();
        ResourceShape child1 = ResourceShape.builder().id("ns.foo#Child1").addResource("ns.foo#Child2").build();
        ResourceShape child2 = ResourceShape.builder().id("ns.foo#Child2").build();
        ShapeIndex shapeIndex = ShapeIndex.builder()
                .addShapes(parent, resource, identifier, child1, child2)
                .addShapes(createOperation, getOperation, updateOperation, deleteOperation, listOperation)
                .addShapes(namedOperation, collectionOperation, putOperation)
                .build();
        NeighborVisitor neighborVisitor = new NeighborVisitor(shapeIndex);
        List<Relationship> relationships = resource.accept(neighborVisitor);

        assertThat(relationships, containsInAnyOrder(
                Relationship.create(parent, RelationshipType.RESOURCE, resource),

                Relationship.create(resource, RelationshipType.BOUND, parent),
                Relationship.create(resource, RelationshipType.IDENTIFIER, identifier),
                Relationship.create(resource, RelationshipType.CREATE, createOperation),
                Relationship.create(resource, RelationshipType.READ, getOperation),
                Relationship.create(resource, RelationshipType.UPDATE, updateOperation),
                Relationship.create(resource, RelationshipType.DELETE, deleteOperation),
                Relationship.create(resource, RelationshipType.LIST, listOperation),
                Relationship.create(resource, RelationshipType.PUT, putOperation),
                Relationship.create(resource, RelationshipType.COLLECTION_OPERATION, createOperation),
                Relationship.create(resource, RelationshipType.COLLECTION_OPERATION, listOperation),
                Relationship.create(resource, RelationshipType.COLLECTION_OPERATION, collectionOperation),
                Relationship.create(resource, RelationshipType.INSTANCE_OPERATION, getOperation),
                Relationship.create(resource, RelationshipType.INSTANCE_OPERATION, updateOperation),
                Relationship.create(resource, RelationshipType.INSTANCE_OPERATION, deleteOperation),
                Relationship.create(resource, RelationshipType.INSTANCE_OPERATION, namedOperation),
                Relationship.create(resource, RelationshipType.INSTANCE_OPERATION, putOperation),
                Relationship.create(resource, RelationshipType.OPERATION, createOperation),
                Relationship.create(resource, RelationshipType.OPERATION, getOperation),
                Relationship.create(resource, RelationshipType.OPERATION, updateOperation),
                Relationship.create(resource, RelationshipType.OPERATION, deleteOperation),
                Relationship.create(resource, RelationshipType.OPERATION, listOperation),
                Relationship.create(resource, RelationshipType.OPERATION, namedOperation),
                Relationship.create(resource, RelationshipType.OPERATION, putOperation),
                Relationship.create(resource, RelationshipType.OPERATION, collectionOperation),
                Relationship.create(resource, RelationshipType.RESOURCE, child1),

                Relationship.create(namedOperation, RelationshipType.BOUND, resource),
                Relationship.create(createOperation, RelationshipType.BOUND, resource),
                Relationship.create(getOperation, RelationshipType.BOUND, resource),
                Relationship.create(updateOperation, RelationshipType.BOUND, resource),
                Relationship.create(deleteOperation, RelationshipType.BOUND, resource),
                Relationship.create(listOperation, RelationshipType.BOUND, resource),
                Relationship.create(putOperation, RelationshipType.BOUND, resource),
                Relationship.create(collectionOperation, RelationshipType.BOUND, resource),

                Relationship.create(child1, RelationshipType.BOUND, resource)
        ));
    }

    @Test
    public void operationShape() {
        StructureShape error = StructureShape.builder().id("ns.foo#Error").build();
        StructureShape input = StructureShape.builder().id("ns.foo#Input").build();
        StructureShape output = StructureShape.builder().id("ns.foo#Output").build();
        OperationShape method = OperationShape.builder()
                .id("ns.foo#Foo")
                .input(input.getId())
                .output(output.getId())
                .addError(error.getId())
                .build();
        ShapeIndex shapeIndex = ShapeIndex.builder().addShapes(method, input, output, error).build();
        NeighborVisitor neighborVisitor = new NeighborVisitor(shapeIndex);
        List<Relationship> relationships = method.accept(neighborVisitor);

        assertThat(relationships, containsInAnyOrder(
                Relationship.create(method, RelationshipType.INPUT, input),
                Relationship.create(method, RelationshipType.OUTPUT, output),
                Relationship.create(method, RelationshipType.ERROR, error)));
    }

    @Test
    public void memberShape() {
        StringShape string = StringShape.builder()
                .id("ns.foo#String")
                .build();
        MemberShape target = MemberShape.builder()
                .id("ns.foo#List$member")
                .target(string.getId())
                .build();
        ListShape list = ListShape.builder()
                .id("ns.foo#List")
                .member(target)
                .build();
        ShapeIndex shapeIndex = ShapeIndex.builder()
                .addShape(target)
                .addShape(string)
                .addShape(list)
                .build();
        NeighborVisitor neighborVisitor = new NeighborVisitor(shapeIndex);
        List<Relationship> relationships = target.accept(neighborVisitor);

        assertThat(relationships, containsInAnyOrder(
                Relationship.create(target, RelationshipType.MEMBER_CONTAINER, list),
                Relationship.create(target, RelationshipType.MEMBER_TARGET, string)));
    }

    @Test
    public void returnsEmptyOnMissingShape() {
        MemberShape target = MemberShape.builder()
                .id("ns.foo#List$member")
                .target("ns.foo#String")
                .build();
        ShapeIndex shapeIndex = ShapeIndex.builder()
                .addShape(target)
                .build();
        NeighborVisitor neighborVisitor = new NeighborVisitor(shapeIndex);
        List<Relationship> relationships = target.accept(neighborVisitor);

        assertThat(relationships, containsInAnyOrder(
                Relationship.createInvalid(target, RelationshipType.MEMBER_CONTAINER, ShapeId.from("ns.foo#List")),
                Relationship.createInvalid(target, RelationshipType.MEMBER_TARGET, ShapeId.from("ns.foo#String"))));
    }
}
