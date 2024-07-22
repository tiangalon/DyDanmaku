// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: google/protobuf/type.proto
// Protobuf Java Version: 4.27.2

package com.google.protobuf;

public interface FieldOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.protobuf.Field)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.google.protobuf.Field.Kind kind = 1 [json_name = "kind"];</code>
   * @return The enum numeric value on the wire for kind.
   */
  int getKindValue();
  /**
   * <code>.google.protobuf.Field.Kind kind = 1 [json_name = "kind"];</code>
   * @return The kind.
   */
  com.google.protobuf.Field.Kind getKind();

  /**
   * <code>.google.protobuf.Field.Cardinality cardinality = 2 [json_name = "cardinality"];</code>
   * @return The enum numeric value on the wire for cardinality.
   */
  int getCardinalityValue();
  /**
   * <code>.google.protobuf.Field.Cardinality cardinality = 2 [json_name = "cardinality"];</code>
   * @return The cardinality.
   */
  com.google.protobuf.Field.Cardinality getCardinality();

  /**
   * <code>int32 number = 3 [json_name = "number"];</code>
   * @return The number.
   */
  int getNumber();

  /**
   * <code>string name = 4 [json_name = "name"];</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 4 [json_name = "name"];</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>string type_url = 6 [json_name = "typeUrl"];</code>
   * @return The typeUrl.
   */
  java.lang.String getTypeUrl();
  /**
   * <code>string type_url = 6 [json_name = "typeUrl"];</code>
   * @return The bytes for typeUrl.
   */
  com.google.protobuf.ByteString
      getTypeUrlBytes();

  /**
   * <code>int32 oneof_index = 7 [json_name = "oneofIndex"];</code>
   * @return The oneofIndex.
   */
  int getOneofIndex();

  /**
   * <code>bool packed = 8 [json_name = "packed"];</code>
   * @return The packed.
   */
  boolean getPacked();

  /**
   * <code>repeated .google.protobuf.Option options = 9 [json_name = "options"];</code>
   */
  java.util.List<com.google.protobuf.Option> 
      getOptionsList();
  /**
   * <code>repeated .google.protobuf.Option options = 9 [json_name = "options"];</code>
   */
  com.google.protobuf.Option getOptions(int index);
  /**
   * <code>repeated .google.protobuf.Option options = 9 [json_name = "options"];</code>
   */
  int getOptionsCount();
  /**
   * <code>repeated .google.protobuf.Option options = 9 [json_name = "options"];</code>
   */
  java.util.List<? extends com.google.protobuf.OptionOrBuilder> 
      getOptionsOrBuilderList();
  /**
   * <code>repeated .google.protobuf.Option options = 9 [json_name = "options"];</code>
   */
  com.google.protobuf.OptionOrBuilder getOptionsOrBuilder(
      int index);

  /**
   * <code>string json_name = 10 [json_name = "jsonName"];</code>
   * @return The jsonName.
   */
  java.lang.String getJsonName();
  /**
   * <code>string json_name = 10 [json_name = "jsonName"];</code>
   * @return The bytes for jsonName.
   */
  com.google.protobuf.ByteString
      getJsonNameBytes();

  /**
   * <code>string default_value = 11 [json_name = "defaultValue"];</code>
   * @return The defaultValue.
   */
  java.lang.String getDefaultValue();
  /**
   * <code>string default_value = 11 [json_name = "defaultValue"];</code>
   * @return The bytes for defaultValue.
   */
  com.google.protobuf.ByteString
      getDefaultValueBytes();
}
