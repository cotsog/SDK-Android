package eu.intent.sdk.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A task is something that must be done by a contractor for a client. A task can be part of an ITOperation. It implements a task template which defines its steps. These steps are represented as an array of ITAction.
 *
 * @see ITAction
 */
public class ITTask implements Parcelable {
    public static final Parcelable.Creator<ITTask> CREATOR = new Parcelable.Creator<ITTask>() {
        public ITTask createFromParcel(Parcel source) {
            return new ITTask(source);
        }

        public ITTask[] newArray(int size) {
            return new ITTask[size];
        }
    };

    public List<ITAction> actions = new ArrayList<>();
    @SerializedName("owner")
    public String client;
    @SerializedName("installer")
    public String contractor;
    @SerializedName("step")
    public int currentStep;
    @SerializedName("_id")
    public String id;
    @SerializedName("updated")
    public long lastUpdate;
    public ITOperation operation;
    public boolean selectAssetOnInstall;
    @SerializedName("totalStep")
    public int stepsCount;
    @SerializedName("taskTemplateId")
    public String templateId;
    @SerializedName("waitingMode")
    public boolean waitingForValidation;

    transient public ITAddress address;
    transient public String assetId;
    transient public ITAssetType assetType;
    transient public String comment;
    transient public boolean commonPart;
    transient public Data data = new Data();
    transient public String door;
    transient public String floor;
    transient public String[] keywords;
    transient public String siteId;

    /**
     * You can put whatever you want in this bundle, for example add properties to this object in order to use it in an adapter.
     * WARNING! Custom classes will not be saved when generating a Parcelable from this object.
     */
    transient public Bundle custom = new Bundle();

    public ITTask() {
    }

    protected ITTask(Parcel in) {
        actions = new ArrayList<>();
        in.readList(actions, ITAction.class.getClassLoader());
        client = in.readString();
        contractor = in.readString();
        currentStep = in.readInt();
        id = in.readString();
        lastUpdate = in.readLong();
        operation = in.readParcelable(ITOperation.class.getClassLoader());
        selectAssetOnInstall = in.readByte() > 0;
        stepsCount = in.readInt();
        templateId = in.readString();
        waitingForValidation = in.readByte() > 0;
        address = in.readParcelable(ITAddress.class.getClassLoader());
        assetId = in.readString();
        int tmpAssetType = in.readInt();
        assetType = tmpAssetType == -1 ? null : ITAssetType.values()[tmpAssetType];
        comment = in.readString();
        commonPart = in.readByte() > 0;
        data = in.readParcelable(Data.class.getClassLoader());
        door = in.readString();
        floor = in.readString();
        keywords = in.createStringArray();
        siteId = in.readString();
        custom = in.readBundle();
    }

    /**
     * Returns true if at least an action of this task is marked with the error flag, false otherwise.
     */
    public boolean hasError() {
        for (ITAction action : actions) {
            if (action.error) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the task is finished, i.e. the current step is after the last step.
     */
    public boolean isFinished() {
        return currentStep >= stepsCount;
    }

    /**
     * Returns true if the task is finished and not in waiting state.
     */
    public boolean isValidated() {
        return isFinished() && !waitingForValidation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(actions);
        dest.writeString(client);
        dest.writeString(contractor);
        dest.writeInt(currentStep);
        dest.writeString(id);
        dest.writeLong(lastUpdate);
        dest.writeParcelable(operation, flags);
        dest.writeByte((byte) (selectAssetOnInstall ? 1 : 0));
        dest.writeInt(stepsCount);
        dest.writeString(templateId);
        dest.writeByte((byte) (waitingForValidation ? 1 : 0));
        dest.writeParcelable(address, 0);
        dest.writeString(assetId);
        dest.writeInt(assetType == null ? -1 : assetType.ordinal());
        dest.writeString(comment);
        dest.writeByte((byte) (commonPart ? 1 : 0));
        dest.writeParcelable(data, flags);
        dest.writeString(door);
        dest.writeString(floor);
        dest.writeStringArray(keywords);
        dest.writeString(siteId);
        dest.writeBundle(custom);
    }

    /**
     * A task can provide additional data. Not all the properties of the Data object may be set, depending on the task template.
     */
    public static class Data implements Parcelable {
        public static final Creator<Data> CREATOR = new Creator<Data>() {
            public Data createFromParcel(Parcel source) {
                return new Data(source);
            }

            public Data[] newArray(int size) {
                return new Data[size];
            }
        };
        public List<String> activities;
        public String deviceId;
        public String deviceType;

        public Data() {
        }

        protected Data(Parcel in) {
            activities = in.createStringArrayList();
            deviceId = in.readString();
            deviceType = in.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeStringList(activities);
            dest.writeString(deviceId);
            dest.writeString(deviceType);
        }
    }

    public static class Deserializer implements JsonDeserializer<ITTask> {
        @Override
        public ITTask deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ITAction.class, new ITAction.Deserializer())
                    .registerTypeAdapter(ITLocation.class, new ITLocation.Deserializer())
                    .create();
            ITTask task = gson.fromJson(json, typeOfT);
            Collections.sort(task.actions, new Comparator<ITAction>() {
                @Override
                public int compare(ITAction a1, ITAction a2) {
                    return Integer.valueOf(a1.index).compareTo(a2.index);
                }
            });
            JsonObject jsonObject = json.getAsJsonObject();
            JsonElement asset = jsonObject.get("asset");
            if (asset != null && asset.isJsonObject()) {
                JsonObject assetObject = asset.getAsJsonObject();
                task.address = gson.fromJson(assetObject.get("address"), ITAddress.class);
                task.assetId = assetObject.has("assetId") && !assetObject.get("assetId").isJsonNull() ? assetObject.get("assetId").getAsString() : "";
                task.assetType = assetObject.has("assetType") && !assetObject.get("assetType").isJsonNull() ?
                        ITAssetType.fromString(assetObject.get("assetType").getAsString()) :
                        ITAssetType.SITE;
                task.commonPart = !assetObject.has("portion") || TextUtils.isEmpty(assetObject.get("portion").getAsString()) || TextUtils.equals(assetObject.get("portion").getAsString(), "commonPortion");
                task.door = assetObject.has("door") && !assetObject.get("door").isJsonNull() ? assetObject.get("door").getAsString() : "";
                task.floor = assetObject.has("level") && !assetObject.get("level").isJsonNull() ? assetObject.get("level").getAsString() : "";
                task.keywords = assetObject.has("keywords") && !assetObject.get("keywords").isJsonNull() ? assetObject.get("keywords").getAsString().split(" ") : new String[0];
                task.siteId = assetObject.has("siteId") && !assetObject.get("siteId").isJsonNull() ? assetObject.get("siteId").getAsString() : "";
            }
            JsonElement infos = jsonObject.get("infos");
            if (infos != null && infos.isJsonObject()) {
                JsonObject infosObject = infos.getAsJsonObject();
                task.comment = infosObject.has("comment") && !infosObject.get("comment").isJsonNull() ? infosObject.get("comment").getAsString() : "";
                task.data.deviceType = infosObject.has("deviceType") ? infosObject.get("deviceType").getAsString() : "";
                task.data.activities = new ArrayList<>();
                if (infosObject.has("activities")) {
                    for (JsonElement activity : infosObject.get("activities").getAsJsonArray()) {
                        task.data.activities.add(activity.getAsString());
                    }
                }
            }
            return task;
        }
    }
}
