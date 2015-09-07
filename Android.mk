LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
  src/com/fastbootmobile/encore/service/IPlaybackCallback.aidl \
  src/com/fastbootmobile/encore/service/IPlaybackService.aidl

LOCAL_STATIC_JAVA_LIBRARIES = android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-appcompat

LOCAL_STATIC_JAVA_LIBRARIES += \
    calligraphy \
    jEN \
    sentry \
    java_websocket \
    json_simple

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += \
    calligraphy:libs/calligraphy-1.1.0.jar \
    jEN:libs/jEN.jar \
    sentry:libs/sentry-1.1.4.jar \
    java_websocket:libs/java_websocket.jar \
    json_simple:libs/json_simple-1.1.jar

LOCAL_PACKAGE_NAME := Encore

LOCAL_DEX_PREOPT := nostripping

LOCAL_JNI_SHARED_LIBRARIES += libnativeplayerjni

include $(BUILD_PACKAGE)


