// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: google/protobuf/any.proto
// Protobuf Java Version: 4.27.2

package com.google.protobuf;

public interface AnyOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.protobuf.Any)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string type_url = 1 [json_name = "typeUrl"];</code>
   * @return The typeUrl.
   */
  java.lang.String getTypeUrl();
  /**
   * <code>string type_url = 1 [json_name = "typeUrl"];</code>
   * @return The bytes for typeUrl.
   */
  com.google.protobuf.ByteString
      getTypeUrlBytes();

  /**
   * <code>bytes value = 2 [json_name = "value"];</code>
   * @return The value.
   */
  com.google.protobuf.ByteString getValue();
}
