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
 * CommPrefsInput
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen")
public class CommPrefsInput {
  public static final String SERIALIZED_NAME_EMAIL = "email";
  @SerializedName(SERIALIZED_NAME_EMAIL)
  private String email;

  public static final String SERIALIZED_NAME_FEATURE_UPDATES = "featureUpdates";
  @SerializedName(SERIALIZED_NAME_FEATURE_UPDATES)
  private Boolean featureUpdates;

  public static final String SERIALIZED_NAME_SECURITY_UPDATES = "securityUpdates";
  @SerializedName(SERIALIZED_NAME_SECURITY_UPDATES)
  private Boolean securityUpdates;

  public CommPrefsInput() {
  }

  public CommPrefsInput email(String email) {
    
    this.email = email;
    return this;
  }

   /**
   * the provided email
   * @return email
  **/
  @javax.annotation.Nullable

  public String getEmail() {
    return email;
  }


  public void setEmail(String email) {
    this.email = email;
  }


  public CommPrefsInput featureUpdates(Boolean featureUpdates) {
    
    this.featureUpdates = featureUpdates;
    return this;
  }

   /**
   * was \&quot;feature updates\&quot; checked
   * @return featureUpdates
  **/
  @javax.annotation.Nonnull

  public Boolean getFeatureUpdates() {
    return featureUpdates;
  }


  public void setFeatureUpdates(Boolean featureUpdates) {
    this.featureUpdates = featureUpdates;
  }


  public CommPrefsInput securityUpdates(Boolean securityUpdates) {
    
    this.securityUpdates = securityUpdates;
    return this;
  }

   /**
   * was \&quot;security updates\&quot; checked
   * @return securityUpdates
  **/
  @javax.annotation.Nonnull

  public Boolean getSecurityUpdates() {
    return securityUpdates;
  }


  public void setSecurityUpdates(Boolean securityUpdates) {
    this.securityUpdates = securityUpdates;
  }



  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CommPrefsInput commPrefsInput = (CommPrefsInput) o;
    return Objects.equals(this.email, commPrefsInput.email) &&
        Objects.equals(this.featureUpdates, commPrefsInput.featureUpdates) &&
        Objects.equals(this.securityUpdates, commPrefsInput.securityUpdates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email, featureUpdates, securityUpdates);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CommPrefsInput {\n");
    sb.append("    email: ").append(toIndentedString(email)).append("\n");
    sb.append("    featureUpdates: ").append(toIndentedString(featureUpdates)).append("\n");
    sb.append("    securityUpdates: ").append(toIndentedString(securityUpdates)).append("\n");
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
    openapiFields.add("email");
    openapiFields.add("featureUpdates");
    openapiFields.add("securityUpdates");

    // a set of required properties/fields (JSON key names)
    openapiRequiredFields = new HashSet<String>();
    openapiRequiredFields.add("featureUpdates");
    openapiRequiredFields.add("securityUpdates");
  }

 /**
  * Validates the JSON Object and throws an exception if issues found
  *
  * @param jsonObj JSON Object
  * @throws IOException if the JSON Object is invalid with respect to CommPrefsInput
  */
  public static void validateJsonObject(JsonObject jsonObj) throws IOException {
      if (jsonObj == null) {
        if (!CommPrefsInput.openapiRequiredFields.isEmpty()) { // has required fields but JSON object is null
          throw new IllegalArgumentException(String.format("The required field(s) %s in CommPrefsInput is not found in the empty JSON string", CommPrefsInput.openapiRequiredFields.toString()));
        }
      }

      Set<Entry<String, JsonElement>> entries = jsonObj.entrySet();
      // check to see if the JSON string contains additional fields
      for (Entry<String, JsonElement> entry : entries) {
        if (!CommPrefsInput.openapiFields.contains(entry.getKey())) {
          throw new IllegalArgumentException(String.format("The field `%s` in the JSON string is not defined in the `CommPrefsInput` properties. JSON: %s", entry.getKey(), jsonObj.toString()));
        }
      }

      // check to make sure all required properties/fields are present in the JSON string
      for (String requiredField : CommPrefsInput.openapiRequiredFields) {
        if (jsonObj.get(requiredField) == null) {
          throw new IllegalArgumentException(String.format("The required field `%s` is not found in the JSON string: %s", requiredField, jsonObj.toString()));
        }
      }
      if ((jsonObj.get("email") != null && !jsonObj.get("email").isJsonNull()) && !jsonObj.get("email").isJsonPrimitive()) {
        throw new IllegalArgumentException(String.format("Expected the field `email` to be a primitive type in the JSON string but got `%s`", jsonObj.get("email").toString()));
      }
  }

  public static class CustomTypeAdapterFactory implements TypeAdapterFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
       if (!CommPrefsInput.class.isAssignableFrom(type.getRawType())) {
         return null; // this class only serializes 'CommPrefsInput' and its subtypes
       }
       final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
       final TypeAdapter<CommPrefsInput> thisAdapter
                        = gson.getDelegateAdapter(this, TypeToken.get(CommPrefsInput.class));

       return (TypeAdapter<T>) new TypeAdapter<CommPrefsInput>() {
           @Override
           public void write(JsonWriter out, CommPrefsInput value) throws IOException {
             JsonObject obj = thisAdapter.toJsonTree(value).getAsJsonObject();
             elementAdapter.write(out, obj);
           }

           @Override
           public CommPrefsInput read(JsonReader in) throws IOException {
             JsonObject jsonObj = elementAdapter.read(in).getAsJsonObject();
             validateJsonObject(jsonObj);
             return thisAdapter.fromJsonTree(jsonObj);
           }

       }.nullSafe();
    }
  }

 /**
  * Create an instance of CommPrefsInput given an JSON string
  *
  * @param jsonString JSON string
  * @return An instance of CommPrefsInput
  * @throws IOException if the JSON string is invalid with respect to CommPrefsInput
  */
  public static CommPrefsInput fromJson(String jsonString) throws IOException {
    return JSON.getGson().fromJson(jsonString, CommPrefsInput.class);
  }

 /**
  * Convert an instance of CommPrefsInput to an JSON string
  *
  * @return JSON string
  */
  public String toJson() {
    return JSON.getGson().toJson(this);
  }
}

