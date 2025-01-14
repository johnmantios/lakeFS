/*
 * lakeFS API
 * lakeFS HTTP API
 *
 * The version of the OpenAPI document: 0.1.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package io.lakefs.clients.api.model;

import java.util.Objects;
import java.util.Arrays;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import io.lakefs.clients.api.JSON;

/**
 * StorageConfig
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class StorageConfig {
  public static final String SERIALIZED_NAME_BLOCKSTORE_TYPE = "blockstore_type";
  @SerializedName(SERIALIZED_NAME_BLOCKSTORE_TYPE)
  private String blockstoreType;

  public static final String SERIALIZED_NAME_BLOCKSTORE_NAMESPACE_EXAMPLE = "blockstore_namespace_example";
  @SerializedName(SERIALIZED_NAME_BLOCKSTORE_NAMESPACE_EXAMPLE)
  private String blockstoreNamespaceExample;

  public static final String SERIALIZED_NAME_BLOCKSTORE_NAMESPACE_VALIDITY_REGEX = "blockstore_namespace_ValidityRegex";
  @SerializedName(SERIALIZED_NAME_BLOCKSTORE_NAMESPACE_VALIDITY_REGEX)
  private String blockstoreNamespaceValidityRegex;

  public static final String SERIALIZED_NAME_DEFAULT_NAMESPACE_PREFIX = "default_namespace_prefix";
  @SerializedName(SERIALIZED_NAME_DEFAULT_NAMESPACE_PREFIX)
  private String defaultNamespacePrefix;

  public static final String SERIALIZED_NAME_PRE_SIGN_SUPPORT = "pre_sign_support";
  @SerializedName(SERIALIZED_NAME_PRE_SIGN_SUPPORT)
  private Boolean preSignSupport;

  public static final String SERIALIZED_NAME_IMPORT_SUPPORT = "import_support";
  @SerializedName(SERIALIZED_NAME_IMPORT_SUPPORT)
  private Boolean importSupport;

  public StorageConfig() {
  }

  public StorageConfig blockstoreType(String blockstoreType) {
    
    this.blockstoreType = blockstoreType;
    return this;
  }

   /**
   * Get blockstoreType
   * @return blockstoreType
  **/
  @javax.annotation.Nonnull

  public String getBlockstoreType() {
    return blockstoreType;
  }


  public void setBlockstoreType(String blockstoreType) {
    this.blockstoreType = blockstoreType;
  }


  public StorageConfig blockstoreNamespaceExample(String blockstoreNamespaceExample) {
    
    this.blockstoreNamespaceExample = blockstoreNamespaceExample;
    return this;
  }

   /**
   * Get blockstoreNamespaceExample
   * @return blockstoreNamespaceExample
  **/
  @javax.annotation.Nonnull

  public String getBlockstoreNamespaceExample() {
    return blockstoreNamespaceExample;
  }


  public void setBlockstoreNamespaceExample(String blockstoreNamespaceExample) {
    this.blockstoreNamespaceExample = blockstoreNamespaceExample;
  }


  public StorageConfig blockstoreNamespaceValidityRegex(String blockstoreNamespaceValidityRegex) {
    
    this.blockstoreNamespaceValidityRegex = blockstoreNamespaceValidityRegex;
    return this;
  }

   /**
   * Get blockstoreNamespaceValidityRegex
   * @return blockstoreNamespaceValidityRegex
  **/
  @javax.annotation.Nonnull

  public String getBlockstoreNamespaceValidityRegex() {
    return blockstoreNamespaceValidityRegex;
  }


  public void setBlockstoreNamespaceValidityRegex(String blockstoreNamespaceValidityRegex) {
    this.blockstoreNamespaceValidityRegex = blockstoreNamespaceValidityRegex;
  }


  public StorageConfig defaultNamespacePrefix(String defaultNamespacePrefix) {
    
    this.defaultNamespacePrefix = defaultNamespacePrefix;
    return this;
  }

   /**
   * Get defaultNamespacePrefix
   * @return defaultNamespacePrefix
  **/
  @javax.annotation.Nullable

  public String getDefaultNamespacePrefix() {
    return defaultNamespacePrefix;
  }


  public void setDefaultNamespacePrefix(String defaultNamespacePrefix) {
    this.defaultNamespacePrefix = defaultNamespacePrefix;
  }


  public StorageConfig preSignSupport(Boolean preSignSupport) {
    
    this.preSignSupport = preSignSupport;
    return this;
  }

   /**
   * Get preSignSupport
   * @return preSignSupport
  **/
  @javax.annotation.Nonnull

  public Boolean getPreSignSupport() {
    return preSignSupport;
  }


  public void setPreSignSupport(Boolean preSignSupport) {
    this.preSignSupport = preSignSupport;
  }


  public StorageConfig importSupport(Boolean importSupport) {
    
    this.importSupport = importSupport;
    return this;
  }

   /**
   * Get importSupport
   * @return importSupport
  **/
  @javax.annotation.Nonnull

  public Boolean getImportSupport() {
    return importSupport;
  }


  public void setImportSupport(Boolean importSupport) {
    this.importSupport = importSupport;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    StorageConfig storageConfig = (StorageConfig) o;
    return Objects.equals(this.blockstoreType, storageConfig.blockstoreType) &&
        Objects.equals(this.blockstoreNamespaceExample, storageConfig.blockstoreNamespaceExample) &&
        Objects.equals(this.blockstoreNamespaceValidityRegex, storageConfig.blockstoreNamespaceValidityRegex) &&
        Objects.equals(this.defaultNamespacePrefix, storageConfig.defaultNamespacePrefix) &&
        Objects.equals(this.preSignSupport, storageConfig.preSignSupport) &&
        Objects.equals(this.importSupport, storageConfig.importSupport);
  }

  @Override
  public int hashCode() {
    return Objects.hash(blockstoreType, blockstoreNamespaceExample, blockstoreNamespaceValidityRegex, defaultNamespacePrefix, preSignSupport, importSupport);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class StorageConfig {\n");
    sb.append("    blockstoreType: ").append(toIndentedString(blockstoreType)).append("\n");
    sb.append("    blockstoreNamespaceExample: ").append(toIndentedString(blockstoreNamespaceExample)).append("\n");
    sb.append("    blockstoreNamespaceValidityRegex: ").append(toIndentedString(blockstoreNamespaceValidityRegex)).append("\n");
    sb.append("    defaultNamespacePrefix: ").append(toIndentedString(defaultNamespacePrefix)).append("\n");
    sb.append("    preSignSupport: ").append(toIndentedString(preSignSupport)).append("\n");
    sb.append("    importSupport: ").append(toIndentedString(importSupport)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }


  public static HashSet<String> openapiFields;
  public static HashSet<String> openapiRequiredFields;

  static {
    // a set of all properties/fields (JSON key names)
    openapiFields = new HashSet<String>();
    openapiFields.add("blockstore_type");
    openapiFields.add("blockstore_namespace_example");
    openapiFields.add("blockstore_namespace_ValidityRegex");
    openapiFields.add("default_namespace_prefix");
    openapiFields.add("pre_sign_support");
    openapiFields.add("import_support");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("blockstore_type");
    openapiRequiredFields.add("blockstore_namespace_example");
    openapiRequiredFields.add("blockstore_namespace_ValidityRegex");
    openapiRequiredFields.add("pre_sign_support");
    openapiRequiredFields.add("import_support");
  }

 /**
  * Validates the JSON Object and throws an exception if issues found
  *
  * @param jsonObj JSON Object
  * @throws IOException if the JSON Object is invalid with respect to StorageConfig
  */
  public static void validateJsonObject(JsonObject jsonObj) throws IOException {
      if (jsonObj == null) {
        if (!StorageConfig.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in StorageConfig is not found in the empty JSON string", StorageConfig.openapiRequiredFields.toString()));
        }
      }

      Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
      // check to see if the JSON string contains additional fields
      for (Entry<String, JsonElement> entry : entries) {
        if (!StorageConfig.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `StorageConfig` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : StorageConfig.openapiRequiredFields) {
        if (jsonObj.get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonObj.toString()));
        }
      }
      if (!jsonObj.get("blockstore_type").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `blockstore_type` to be a primitive type in the JSON string but got `%s`", jsonObj.get("blockstore_type").toString()));
      }
      if (!jsonObj.get("blockstore_namespace_example").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `blockstore_namespace_example` to be a primitive type in the JSON string but got `%s`", jsonObj.get("blockstore_namespace_example").toString()));
      }
      if (!jsonObj.get("blockstore_namespace_ValidityRegex").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `blockstore_namespace_ValidityRegex` to be a primitive type in the JSON string but got `%s`", jsonObj.get("blockstore_namespace_ValidityRegex").toString()));
      }
      if ((jsonObj.get("default_namespace_prefix") != null && !jsonObj.get("default_namespace_prefix").isJsonNull()) && !jsonObj.get("default_namespace_prefix").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `default_namespace_prefix` to be a primitive type in the JSON string but got `%s`", jsonObj.get("default_namespace_prefix").toString()));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!StorageConfig.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'StorageConfig' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<StorageConfig> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(StorageConfig.class));

       return (TypeAdapter<T>) new TypeAdapter<StorageConfig>() {
           @Override
           public void write(JsonWriter out, StorageConfig value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public StorageConfig read(JsonReader in) throws IOException {
             JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
             validateJsonObject(jsonObj);
             return thisAdapter.fromJsonTree(jsonObj);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of StorageConfig given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of StorageConfig
  * @throws IOException if the JSON string is invalid with respect to StorageConfig
  */
  public static StorageConfig fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, StorageConfig.class);
  }

 /**
  * Convert an instance of StorageConfig to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

