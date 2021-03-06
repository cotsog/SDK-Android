package eu.intent.sdk.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;

/**
 * A template defines a set of states in which a part of a site can be at a given time.
 * <p/>
 * For example we can define a template "overheating" based on the function "threshold crossing", on the activity "temperature". The states of this template would be "ON" or "OFF".
 *
 * @see ITState
 */
public class ITStateTemplate implements Parcelable {
    public static final Parcelable.Creator<ITStateTemplate> CREATOR = new Parcelable.Creator<ITStateTemplate>() {
        public ITStateTemplate createFromParcel(Parcel source) {
            return new ITStateTemplate(source);
        }

        public ITStateTemplate[] newArray(int size) {
            return new ITStateTemplate[size];
        }
    };

    @SerializedName("fnName")
    public String function;
    @SerializedName("templateId")
    public String id;
    @SerializedName("templateLabel")
    public String label;
    public String statusDefault;
    public String[] statusEnum;

    transient public String[] activities;

    /**
     * You can put whatever you want in this bundle, for example add properties to this object in order to use it in an adapter.
     * WARNING! Custom classes will not be saved when generating a Parcelable from this object.
     */
    transient public Bundle custom = new Bundle();

    public ITStateTemplate() {
        // Needed by Retrofit
    }

    protected ITStateTemplate(Parcel in) {
        activities = in.createStringArray();
        id = in.readString();
        label = in.readString();
        statusDefault = in.readString();
        statusEnum = in.createStringArray();
        custom = in.readBundle();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(activities);
        dest.writeString(id);
        dest.writeString(label);
        dest.writeString(statusDefault);
        dest.writeStringArray(statusEnum);
        dest.writeBundle(custom);
    }

    public static class Deserializer implements JsonDeserializer<ITStateTemplate> {
        @Override
        public ITStateTemplate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Gson gson = new Gson();
            ITStateTemplate template = gson.fromJson(json, typeOfT);
            String[] hierarchy = {"filters", "properties", "stream", "properties", "tags", "properties", "id", "properties", "activityKey"};
            JsonObject jsonObject = json.getAsJsonObject();
            for (String property : hierarchy) {
                jsonObject = jsonObject.getAsJsonObject(property);
                if (jsonObject == null) {
                    break;
                }
            }
            if (jsonObject != null) {
                template.activities = gson.fromJson(jsonObject.getAsJsonArray("enum"), String[].class);
            }
            if (template.activities == null) {
                template.activities = new String[0];
            }
            return template;
        }
    }
}
