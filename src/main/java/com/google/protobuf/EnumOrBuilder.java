// Generated by the protocol buffer compiler.  DO NOT EDIT!
// NO CHECKED-IN PROTOBUF GENCODE
// source: google/protobuf/type.proto
// Protobuf Java Version: 4.27.2

package com.google.protobuf;

public interface EnumOrBuilder extends
    // @@protoc_insertion_point(interface_extends:google.protobuf.Enum)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>string name = 1 [json_name = "name"];</code>
   * @return The name.
   */
  java.lang.String getName();
  /**
   * <code>string name = 1 [json_name = "name"];</code>
   * @return The bytes for name.
   */
  com.google.protobuf.ByteString
      getNameBytes();

  /**
   * <code>repeated .google.protobuf.EnumValue enumvalue = 2 [json_name = "enumvalue"];</code>
   */
  java.util.List<com.google.protobuf.EnumValue> 
      getEnumvalueList();
  /**
   * <code>repeated .google.protobuf.EnumValue enumvalue = 2 [json_name = "enumvalue"];</code>
   */
  com.google.protobuf.EnumValue getEnumvalue(int index);
  /**
   * <code>repeated .google.protobuf.EnumValue enumvalue = 2 [json_name = "enumvalue"];</code>
   */
  int getEnumvalueCount();
  /**
   * <code>repeated .google.protobuf.EnumValue enumvalue = 2 [json_name = "enumvalue"];</code>
   */
  java.util.List<? extends com.google.protobuf.EnumValueOrBuilder> 
      getEnumvalueOrBuilderList();
  /**
   * <code>repeated .google.protobuf.EnumValue enumvalue = 2 [json_name = "enumvalue"];</code>
   */
  com.google.protobuf.EnumValueOrBuilder getEnumvalueOrBuilder(
      int index);

  /**
   * <code>repeated .google.protobuf.Option options = 3 [json_name = "options"];</code>
   */
  java.util.List<com.google.protobuf.Option> 
      getOptionsList();
  /**
   * <code>repeated .google.protobuf.Option options = 3 [json_name = "options"];</code>
   */
  com.google.protobuf.Option getOptions(int index);
  /**
   * <code>repeated .google.protobuf.Option options = 3 [json_name = "options"];</code>
   */
  int getOptionsCount();
  /**
   * <code>repeated .google.protobuf.Option options = 3 [json_name = "options"];</code>
   */
  java.util.List<? extends com.google.protobuf.OptionOrBuilder> 
      getOptionsOrBuilderList();
  /**
   * <code>repeated .google.protobuf.Option options = 3 [json_name = "options"];</code>
   */
  com.google.protobuf.OptionOrBuilder getOptionsOrBuilder(
      int index);

  /**
   * <code>.google.protobuf.SourceContext source_context = 4 [json_name = "sourceContext"];</code>
   * @return Whether the sourceContext field is set.
   */
  boolean hasSourceContext();
  /**
   * <code>.google.protobuf.SourceContext source_context = 4 [json_name = "sourceContext"];</code>
   * @return The sourceContext.
   */
  com.google.protobuf.SourceContext getSourceContext();
  /**
   * <code>.google.protobuf.SourceContext source_context = 4 [json_name = "sourceContext"];</code>
   */
  com.google.protobuf.SourceContextOrBuilder getSourceContextOrBuilder();

  /**
   * <code>.google.protobuf.Syntax syntax = 5 [json_name = "syntax"];</code>
   * @return The enum numeric value on the wire for syntax.
   */
  int getSyntaxValue();
  /**
   * <code>.google.protobuf.Syntax syntax = 5 [json_name = "syntax"];</code>
   * @return The syntax.
   */
  com.google.protobuf.Syntax getSyntax();

  /**
   * <code>string edition = 6 [json_name = "edition"];</code>
   * @return The edition.
   */
  java.lang.String getEdition();
  /**
   * <code>string edition = 6 [json_name = "edition"];</code>
   * @return The bytes for edition.
   */
  com.google.protobuf.ByteString
      getEditionBytes();
}
